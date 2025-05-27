package startwithco.startwithbackend.payment.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import startwithco.startwithbackend.common.service.CommonService;
import startwithco.startwithbackend.exception.badRequest.BadRequestErrorResult;
import startwithco.startwithbackend.exception.badRequest.BadRequestException;
import startwithco.startwithbackend.exception.conflict.ConflictErrorResult;
import startwithco.startwithbackend.exception.conflict.ConflictException;
import startwithco.startwithbackend.exception.notFound.NotFoundErrorResult;
import startwithco.startwithbackend.exception.notFound.NotFoundException;
import startwithco.startwithbackend.exception.server.ServerErrorResult;
import startwithco.startwithbackend.exception.server.ServerException;
import startwithco.startwithbackend.payment.payment.domain.PaymentEntity;
import startwithco.startwithbackend.payment.payment.repository.PaymentEntityRepository;
import startwithco.startwithbackend.payment.payment.util.PAYMENT_STATUS;
import startwithco.startwithbackend.payment.paymentEvent.domain.PaymentEventEntity;
import startwithco.startwithbackend.payment.paymentEvent.repository.PaymentEventEntityRepository;
import startwithco.startwithbackend.payment.paymentEvent.util.PAYMENT_EVENT_STATUS;

import java.time.LocalDateTime;

import static startwithco.startwithbackend.payment.payment.controller.request.PaymentRequest.*;
import static startwithco.startwithbackend.payment.payment.controller.response.PaymentResponse.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentEntityRepository paymentEntityRepository;
    private final PaymentEventEntityRepository paymentEventEntityRepository;
    private final CommonService commonService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public Mono<TossPaymentApprovalResponse> tossPaymentApproval(TossPaymentApprovalRequest request) {
        /*
         * [예외 처리]
         * 1. PaymentEntity 유효성 검사
         * 2. PaymentEventStatus가 REQUESTED가 아닐 경우
         * 3. 결제 금액 불일치
         * 4. 결제 승인 실패 떴지만 orderId가 동일할 경우
         */
        PaymentEventEntity paymentEventEntity = paymentEventEntityRepository.findByPaymentEventSeq(request.paymentEventSeq())
                .orElseThrow(() -> new NotFoundException(NotFoundErrorResult.PAYMENT_EVENT_NOT_FOUND_EXCEPTION));

        if (paymentEventEntity.getPaymentEventStatus() != PAYMENT_EVENT_STATUS.REQUESTED) {
            throw new ConflictException(ConflictErrorResult.INVALID_PAYMENT_EVENT_STATUS_CONFLICT_EXCEPTION);
        }

        if (!paymentEventEntity.getAmount().equals(request.amount())) {
            throw new BadRequestException(BadRequestErrorResult.AMOUNT_MISMATCH_BAD_REQUEST_EXCEPTION);
        }

        if (!paymentEntityRepository.canApproveTossPayment(request.orderId(), request.paymentEventSeq())) {
            throw new BadRequestException(BadRequestErrorResult.ORDER_ID_DUPLICATED_BAD_REQUEST_EXCEPTION);
        }

        PaymentEntity paymentEntity = PaymentEntity.builder()
                .paymentEventEntity(paymentEventEntity)
                .orderId(request.orderId())
                .paymentKey(request.paymentKey())
                .amount(request.amount())
                .paymentStatus(PAYMENT_STATUS.IN_PROGRESS)
                .build();

        paymentEntityRepository.savePaymentEntity(paymentEntity);

        return commonService.executeTossPaymentApproval(
                request.paymentKey(),
                request.orderId(),
                request.amount()
        ).flatMap(res -> {
            try {
                JsonNode json = objectMapper.readTree(res);

                paymentEntity.updateSuccessStatus();
                paymentEntityRepository.savePaymentEntity(paymentEntity);

                /*
                * TODO
                *  PAYMENT EVENT STATUS 는 어떻게 수정?
                *  결제 승인되면 구매확정으로?
                * */

                return Mono.just(new TossPaymentApprovalResponse(
                        json.get("orderId").asText(),
                        json.get("orderName").asText(),
                        json.get("paymentKey").asText(),
                        json.get("method").asText(),
                        json.get("totalAmount").asInt(),
                        json.get("approvedAt").asText(),
                        json.get("receipt").get("url").asText()
                ));
            } catch (Exception e) {
                return Mono.error(new ServerException(ServerErrorResult.INTERNAL_SERVER_EXCEPTION));
            }
        }).onErrorResume(e -> {
            PaymentEntity failedPaymentEntity = paymentEntityRepository.findByOrderId(request.orderId())
                    .orElse(null);

            try {
                if (failedPaymentEntity != null) {
                    failedPaymentEntity.updateFailureStatus();
                    paymentEntityRepository.savePaymentEntity(failedPaymentEntity);

                    PaymentEventEntity failedPaymentEventEntity = failedPaymentEntity.getPaymentEventEntity();
                    failedPaymentEventEntity.updatePaymentEventStatus(PAYMENT_EVENT_STATUS.REQUESTED);
                    paymentEventEntityRepository.savePaymentEventEntity(failedPaymentEventEntity);
                }
            } catch (Exception ex) {
                return Mono.error(new ServerException(ServerErrorResult.INTERNAL_SERVER_EXCEPTION));
            }

            return Mono.error(new ServerException(ServerErrorResult.INTERNAL_SERVER_EXCEPTION));
        });
    }
}
