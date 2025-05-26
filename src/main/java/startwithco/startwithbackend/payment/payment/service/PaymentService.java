package startwithco.startwithbackend.payment.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
import startwithco.startwithbackend.payment.payment.util.STATUS;
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
         * 1. 존재하지 않는 결제 이벤트: 404 PAYMENT_EVENT_NOT_FOUND_EXCEPTION
         * 2. 결제 이벤트 상태가 REQUESTED가 아님: 409 INVALID_PAYMENT_EVENT_STATUS_CONFLICT_EXCEPTION
         * 3. 결제 금액 불일치: 400 AMOUNT_MISMATCH_BAD_REQUEST_EXCEPTION
         * 4. 중복 결제(orderId 중복): 409 IDEMPOTENT_REQUEST_CONFLICT_EXCEPTION
         * 5. 결제 승인 응답 파싱 실패: 500 INTERNAL_SERVER_EXCEPTION
         * 6. 기타 서버 오류 → 결제 상태 FAILURE로 갱신 시도: 500 INTERNAL_SERVER_EXCEPTION
         */

        return Mono.fromCallable(() -> {
            // [프로세스 1] 결제 이벤트 조회 및 유효성 검사
            PaymentEventEntity paymentEventEntity = paymentEventEntityRepository.findByPaymentEventSeq(request.paymentEventSeq())
                    .orElseThrow(() -> new NotFoundException(NotFoundErrorResult.PAYMENT_EVENT_NOT_FOUND_EXCEPTION));

            if (paymentEventEntity.getPaymentEventStatus() != PAYMENT_EVENT_STATUS.REQUESTED) {
                throw new ConflictException(ConflictErrorResult.INVALID_PAYMENT_EVENT_STATUS_CONFLICT_EXCEPTION);
            }

            if (!paymentEventEntity.getAmount().equals(request.amount())) {
                throw new BadRequestException(BadRequestErrorResult.AMOUNT_MISMATCH_BAD_REQUEST_EXCEPTION);
            }

            // [프로세스 2] 결제 정보 선 저장 (EXECUTED 상태)
            PaymentEntity paymentEntity = PaymentEntity.builder()
                    .paymentEventEntity(paymentEventEntity)
                    .orderId(request.orderId())
                    .paymentKey(request.paymentKey())
                    .amount(request.amount())
                    .status(STATUS.EXECUTED)
                    .build();

            try {
                paymentEntityRepository.savePaymentEntity(paymentEntity);
            } catch (DataIntegrityViolationException e) {
                throw new ConflictException(ConflictErrorResult.IDEMPOTENT_REQUEST_CONFLICT_EXCEPTION);
            }

            return paymentEntity;
        }).flatMap(paymentEntity ->
                // [프로세스 3] PG사 결제 승인 요청 및 응답 파싱
                commonService.executeTossPaymentApproval(
                        paymentEntity.getPaymentKey(),
                        paymentEntity.getOrderId(),
                        paymentEntity.getAmount()
                ).flatMap(res -> {
                    try {
                        JsonNode json = objectMapper.readTree(res);

                        // [프로세스 4] 결제 상태 COMPLETED로 변경 및 응답 생성
                        paymentEntity.updateStatus(STATUS.COMPLETED);
                        paymentEntityRepository.savePaymentEntity(paymentEntity);

                        PaymentEventEntity paymentEventEntity = paymentEntity.getPaymentEventEntity();
                        paymentEventEntity.updatePaymentEventStatus(PAYMENT_EVENT_STATUS.DEVELOPING);
                        paymentEventEntity.updatePaymentCompletedAt(LocalDateTime.now());
                        paymentEventEntityRepository.savePaymentEventEntity(paymentEventEntity);

                        TossPaymentApprovalResponse response = new TossPaymentApprovalResponse(
                                json.get("orderId").asText(),
                                json.get("orderName").asText(),
                                json.get("paymentKey").asText(),
                                json.get("method").asText(),
                                json.get("totalAmount").asInt(),
                                json.get("approvedAt").asText(),
                                json.get("receipt").get("url").asText()
                        );

                        return Mono.just(response);
                    } catch (Exception e) {
                        return Mono.error(new ServerException(ServerErrorResult.INTERNAL_SERVER_EXCEPTION));
                    }
                })
        ).onErrorResume(e -> {
            // [프로세스 5] 서버 예외 발생 시 결제 상태 FAILURE로 갱신 시도
            if (e instanceof ConflictException || e instanceof BadRequestException || e instanceof NotFoundException) {
                return Mono.error(e);
            }

            try {
                PaymentEntity failedPayment = paymentEntityRepository.findByOrderId(request.orderId()).orElse(null);

                if (failedPayment != null) {
                    failedPayment.updateStatus(STATUS.FAILURE);
                    paymentEntityRepository.savePaymentEntity(failedPayment);

                    PaymentEventEntity failedPaymentEventEntity = failedPayment.getPaymentEventEntity();
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
