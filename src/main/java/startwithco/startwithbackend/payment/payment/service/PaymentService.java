package startwithco.startwithbackend.payment.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import startwithco.startwithbackend.common.service.CommonService;
import startwithco.startwithbackend.exception.BadRequestException;
import startwithco.startwithbackend.exception.NotFoundException;
import startwithco.startwithbackend.exception.ServerException;
import startwithco.startwithbackend.payment.payment.domain.PaymentEntity;
import startwithco.startwithbackend.payment.payment.repository.PaymentEntityRepository;
import startwithco.startwithbackend.payment.payment.util.PAYMENT_STATUS;
import startwithco.startwithbackend.payment.paymentEvent.domain.PaymentEventEntity;
import startwithco.startwithbackend.payment.paymentEvent.repository.PaymentEventEntityRepository;
import startwithco.startwithbackend.payment.paymentEvent.util.PAYMENT_EVENT_STATUS;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.*;
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
         * 4. 이미 결제 요청에 대해 결제 승인이 된 경우
         */
        PaymentEventEntity paymentEventEntity = paymentEventEntityRepository.findByPaymentEventSeq(request.paymentEventSeq())
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 결제 요청입니다.",
                        getCode("존재하지 않는 결제 요청입니다.", ExceptionType.NOT_FOUND)
                ));
        if (paymentEventEntity.getPaymentEventStatus() != PAYMENT_EVENT_STATUS.REQUESTED) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "결제 요청이 REQUEST 상태가 아닙니다.",
                    getCode("결제 요청이 REQUEST 상태가 아닙니다.", ExceptionType.BAD_REQUEST)
            );
        }
        if (!paymentEventEntity.getAmount().equals(request.amount())) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "결제 금액이 TOSS PAYMENT 승인 금액과 다릅니다.",
                    getCode("결제 금액이 TOSS PAYMENT 승인 금액과 다릅니다.", ExceptionType.BAD_REQUEST)
            );
        }
        if (!paymentEntityRepository.canApproveTossPayment(request.orderId(), request.paymentEventSeq())) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "해당 결제 요청은 승인할 수 없습니다. 유효하지 않은 주문이거나 이미 처리된 결제입니다.",
                    getCode("해당 결제 요청은 승인할 수 없습니다. 유효하지 않은 주문이거나 이미 처리된 결제입니다.", ExceptionType.BAD_REQUEST)
            );
        }

        PaymentEntity paymentEntity;
        if (paymentEntityRepository.canSavePaymentEntity(request.paymentEventSeq())) {
            paymentEntity = PaymentEntity.builder()
                    .paymentEventEntity(paymentEventEntity)
                    .orderId(paymentEventEntity.getOrderId())
                    .paymentKey(request.paymentKey())
                    .amount(request.amount())
                    .paymentStatus(PAYMENT_STATUS.IN_PROGRESS)
                    .build();

            paymentEntityRepository.savePaymentEntity(paymentEntity);
        } else {
            throw new ServerException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "중복된 결제 데이터가 존재합니다.",
                    getCode("중복된 결제 데이터가 존재합니다.", ExceptionType.SERVER)
            );
        }

        return commonService.executeTossPaymentApproval(
                request.paymentKey(),
                request.orderId(),
                request.amount()
        ).flatMap(res -> {
            try {
                JsonNode json = objectMapper.readTree(res);
                String approvedAtStr = json.get("approvedAt").asText();
                OffsetDateTime offsetDateTime = OffsetDateTime.parse(approvedAtStr);
                LocalDateTime approvedAt = offsetDateTime.toLocalDateTime();

                paymentEntity.updateSuccessStatus(approvedAt);
                paymentEntityRepository.savePaymentEntity(paymentEntity);

                paymentEventEntity.updatePaymentEventStatus(PAYMENT_EVENT_STATUS.CONFIRMED);
                paymentEventEntityRepository.savePaymentEventEntity(paymentEventEntity);

                return Mono.just(new TossPaymentApprovalResponse(
                        json.get("orderId").asText(),
                        json.get("orderName").asText(),
                        json.get("paymentKey").asText(),
                        json.get("method").asText(),
                        json.get("totalAmount").asInt(),
                        approvedAt,
                        json.get("receipt").get("url").asText()
                ));
            } catch (Exception e) {
                return Mono.error(new ServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "결제 응답 파싱 중 오류가 발생했습니다.",
                        getCode("결제 응답 파싱 중 오류가 발생했습니다.", ExceptionType.SERVER)
                ));
            }
        }).onErrorResume(e -> {
            paymentEntity.updateFailureStatus();
            paymentEntityRepository.savePaymentEntity(paymentEntity);

            PaymentEventEntity failedPaymentEventEntity = paymentEntity.getPaymentEventEntity();
            failedPaymentEventEntity.updatePaymentEventStatus(PAYMENT_EVENT_STATUS.REQUESTED);
            String orderId = UUID.randomUUID().toString();
            failedPaymentEventEntity.updatePaymentEventOrderId(orderId);
            paymentEventEntityRepository.savePaymentEventEntity(failedPaymentEventEntity);

            return Mono.error(new ServerException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "내부 서버 오류가 발생했습니다.",
                    getCode("내부 서버 오류가 발생했습니다.", ExceptionType.SERVER)
            ));
        });
    }
}
