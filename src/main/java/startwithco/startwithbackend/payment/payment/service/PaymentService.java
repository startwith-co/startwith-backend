package startwithco.startwithbackend.payment.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
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
import startwithco.startwithbackend.payment.payment.util.METHOD;
import startwithco.startwithbackend.payment.payment.util.PAYMENT_STATUS;
import startwithco.startwithbackend.payment.paymentEvent.domain.PaymentEventEntity;
import startwithco.startwithbackend.payment.paymentEvent.repository.PaymentEventEntityRepository;
import startwithco.startwithbackend.payment.paymentEvent.util.PAYMENT_EVENT_STATUS;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
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

    @Transactional
    public Mono<?> tossPaymentApproval(TossPaymentApprovalRequest request) {
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
        if (!paymentEventEntity.getActualAmount().equals(request.amount())) {
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
        ).flatMap(json -> {
            try {
                String method = json.get("method").asText();

                if ("카드".equals(method)) {
                    String approvedAtStr = json.get("approvedAt").asText();
                    LocalDateTime approvedAt = OffsetDateTime.parse(approvedAtStr).toLocalDateTime();

                    paymentEntity.updateMethod(METHOD.CARD);
                    paymentEntity.updateSuccessStatus(approvedAt);
                    paymentEntityRepository.savePaymentEntity(paymentEntity);

                    paymentEventEntity.updatePaymentEventStatus(PAYMENT_EVENT_STATUS.CONFIRMED);
                    paymentEventEntityRepository.savePaymentEventEntity(paymentEventEntity);

                    JsonNode cardNode = json.get("card");
                    return Mono.just(new TossCardPaymentApprovalResponse(
                            json.get("orderId").asText(),
                            json.get("orderName").asText(),
                            json.get("paymentKey").asText(),
                            method,
                            json.get("totalAmount").asInt(),
                            approvedAt,
                            cardNode != null && cardNode.get("issuerCode") != null ? cardNode.get("issuerCode").asText() : null,
                            cardNode != null && cardNode.get("number") != null ? cardNode.get("number").asText() : null,
                            cardNode != null && cardNode.get("cardType") != null ? cardNode.get("cardType").asText() : null,
                            json.get("receipt").get("url").asText()
                    ));
                } else if ("가상계좌".equals(method)) {
                    paymentEntity.updateMethod(METHOD.VIRTUAL_ACCOUNT);
                    paymentEntity.updateSecret(json.get("secret").asText());
                    paymentEntityRepository.savePaymentEntity(paymentEntity);

                    JsonNode vaNode = json.get("virtualAccount");
                    String requestedAtStr = json.get("requestedAt").asText();
                    String dueDateStr = vaNode.get("dueDate").asText();

                    return Mono.just(new TossVirtualAccountPaymentResponse(
                            json.get("orderId").asText(),
                            json.get("orderName").asText(),
                            json.get("paymentKey").asText(),
                            method,
                            json.get("totalAmount").asInt(),
                            OffsetDateTime.parse(requestedAtStr).toLocalDateTime(),
                            vaNode.get("accountNumber").asText(),
                            vaNode.get("bankCode").asText(),
                            vaNode.get("customerName").asText(),
                            OffsetDateTime.parse(dueDateStr).toLocalDateTime(),
                            json.has("cashReceipt") && json.get("cashReceipt").has("receiptUrl") ?
                                    json.get("cashReceipt").get("receiptUrl").asText() : null,
                            json.get("secret").asText(),
                            json.get("receipt").get("url").asText()
                    ));
                } else {
                    return Mono.error(new ServerException(
                            HttpStatus.BAD_REQUEST.value(),
                            "지원하지 않는 결제 수단입니다: " + method,
                            getCode("지원하지 않는 결제 수단입니다", ExceptionType.BAD_REQUEST)
                    ));
                }
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

            paymentEventEntity.updatePaymentEventStatus(PAYMENT_EVENT_STATUS.REQUESTED);
            paymentEventEntity.updatePaymentEventOrderId(UUID.randomUUID().toString());
            paymentEventEntityRepository.savePaymentEventEntity(paymentEventEntity);

            return Mono.error(new ServerException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    e.getMessage(),
                    getCode(e.getMessage(), ExceptionType.SERVER)
            ));
        });
    }

    public void tossPaymentDepositCallBack(TossPaymentDepositCallBackRequest request) {
        PaymentEntity paymentEntity = paymentEntityRepository.findBySecret(request.secret())
                .orElseThrow(() -> new ServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "무통장 입금 전 결제가 저장되지 않았습니다.",
                        getCode("무통장 입금 전 결제가 저장되지 않았습니다.", ExceptionType.SERVER)
                ));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        LocalDateTime paymentCompletedAt = LocalDateTime.parse(request.createdAt(), formatter);
        paymentEntity.updateSuccessStatus(paymentCompletedAt);
        paymentEntityRepository.savePaymentEntity(paymentEntity);
    }
}
