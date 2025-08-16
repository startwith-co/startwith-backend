package startwithco.startwithbackend.payment.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import startwithco.startwithbackend.common.service.CommonService;
import startwithco.startwithbackend.exception.BadRequestException;
import startwithco.startwithbackend.exception.ConflictException;
import startwithco.startwithbackend.exception.NotFoundException;
import startwithco.startwithbackend.exception.ServerException;
import startwithco.startwithbackend.payment.payment.domain.PaymentEntity;
import startwithco.startwithbackend.payment.payment.repository.PaymentEntityRepository;
import startwithco.startwithbackend.payment.payment.util.METHOD;
import startwithco.startwithbackend.payment.payment.util.PAYMENT_STATUS;
import startwithco.startwithbackend.payment.paymentEvent.domain.PaymentEventEntity;
import startwithco.startwithbackend.payment.paymentEvent.repository.PaymentEventEntityRepository;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

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
                    "해당 결제 요청은 승인할 수 없습니다. 결제 승인 진행 중입니다.",
                    getCode("해당 결제 요청은 승인할 수 없습니다. 결제 승인 진행 중입니다.", ExceptionType.BAD_REQUEST)
            );
        }

        PaymentEntity paymentEntity = PaymentEntity.builder()
                .paymentEventEntity(paymentEventEntity)
                .orderId(request.orderId())
                .paymentKey(request.paymentKey())
                .amount(request.amount())
                .paymentStatus(PAYMENT_STATUS.IN_PROGRESS)
                .build();

        try {
            paymentEntityRepository.savePaymentEntity(paymentEntity);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(
                    HttpStatus.CONFLICT.value(),
                    "이미 해당 결제 요청에 대한 결제 정보가 존재합니다. 새롭게 결제 요청을 진행해야합니다.",
                    getCode("이미 해당 결제 요청에 대한 결제 정보가 존재합니다. 새롭게 결제 요청을 진행해야합니다.", ExceptionType.CONFLICT)
            );
        }

        return commonService.executeTossPaymentApproval(
                request.paymentKey(),
                request.orderId(),
                request.amount()
        ).flatMap(json -> {
            try {
                String method = json.path("method").asText();

                if ("카드".equals(method)) {
                    LocalDateTime approvedAt = OffsetDateTime.parse(json.path("approvedAt").asText()).toLocalDateTime();
                    paymentEntity.updateCardDONEStatus(approvedAt);
                    paymentEntityRepository.savePaymentEntity(paymentEntity);

                    JsonNode card = json.path("card");
                    return Mono.just(new TossCardPaymentApprovalResponse(
                            json.path("orderId").asText(),
                            json.path("orderName").asText(),
                            json.path("paymentKey").asText(),
                            method,
                            json.path("totalAmount").asInt(),
                            approvedAt,
                            card.path("issuerCode").asText(null),
                            card.path("number").asText(null),
                            card.path("cardType").asText(null),
                            json.path("receipt").path("url").asText(),
                            paymentEventEntity.getSolutionEntity().getCategory()
                    ));
                }

                if ("가상계좌".equals(method)) {
                    LocalDateTime requestedAt = OffsetDateTime.parse(json.path("requestedAt").asText()).toLocalDateTime();
                    String secret = json.path("secret").asText(null);

                    paymentEntity.updateVirtualWAITING_FOR_DEPOSITStatus(requestedAt, secret);
                    paymentEntityRepository.savePaymentEntity(paymentEntity);

                    JsonNode va = json.path("virtualAccount");
                    return Mono.just(new TossVirtualAccountPaymentResponse(
                            json.path("orderId").asText(),
                            json.path("orderName").asText(),
                            json.path("paymentKey").asText(),
                            method,
                            json.path("totalAmount").asInt(),
                            requestedAt,
                            va.path("accountNumber").asText(),
                            va.path("bankCode").asText(),
                            va.path("customerName").asText(),
                            requestedAt.plusDays(1),
                            json.path("cashReceipt").path("receiptUrl").asText(null),
                            secret,
                            json.path("receipt").path("url").asText(),
                            paymentEventEntity.getSolutionEntity().getCategory()
                    ));
                }

                if ("간편결제".equals(method)) {
                    paymentEntity.updateEasyPayDONEStatus();
                    paymentEntityRepository.savePaymentEntity(paymentEntity);

                    return Mono.just(new TossEasyPayPaymentApprovalResponse(
                            json.path("orderId").asText(),
                            json.path("orderName").asText(),
                            json.path("paymentKey").asText(),
                            method,
                            json.path("totalAmount").asInt(),
                            LocalDateTime.now(),
                            json.path("receipt").path("url").asText(null),
                            paymentEventEntity.getSolutionEntity().getCategory()
                    ));
                }

                return Mono.error(new ServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "지원하지 않는 결제 수단입니다.",
                        getCode("지원하지 않는 결제 수단입니다.", ExceptionType.SERVER)
                ));

            } catch (Exception e) {
                return Mono.error(new ServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        e.getMessage(),
                        getCode(e.getMessage(), ExceptionType.SERVER)
                ));
            }
        }).onErrorResume(e -> {
            paymentEntity.updateFAILUREStatus();
            paymentEntityRepository.savePaymentEntity(paymentEntity);

            return Mono.error(new ServerException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    e.getMessage(),
                    getCode(e.getMessage(), ExceptionType.SERVER)
            ));
        });
    }

    @Transactional
    public void refundTossPaymentApprovalRequest(RefundTossPaymentApprovalRequest request) {
        PaymentEntity paymentEntity = paymentEntityRepository.findByOrderId(request.orderId())
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 결제입니다.",
                        getCode("존재하지 않는 결제입니다.", ExceptionType.NOT_FOUND)
                ));

        if (paymentEntity.getDueDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "해당 결제는 마감 시간이 지났습니다.",
                    getCode("해당 결제는 마감 시간이 지났습니다.", ExceptionType.BAD_REQUEST)
            );
        }

        if(paymentEntity.getPaymentStatus() == PAYMENT_STATUS.CANCELLED) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "이미 환불 처리된 건입니다.",
                    getCode("이미 환불 처리된 건입니다.", ExceptionType.BAD_REQUEST)
            );
        }

        if (!(paymentEntity.getMethod() == METHOD.VIRTUAL_ACCOUNT
                && paymentEntity.getPaymentStatus() == PAYMENT_STATUS.DONE)) {
            if (request.bankCode() != null || request.accountNumber() != null || request.holderName() != null) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "가상계좌 결제 완료 상태가 아닐 경우 환불 계좌 정보를 입력할 수 없습니다.",
                        getCode("가상계좌 결제 완료 상태가 아닐 경우 환불 계좌 정보를 입력할 수 없습니다.", ExceptionType.BAD_REQUEST)
                );
            }
        }

        String bankCode = null;
        String accountNumber = null;
        String holderName = null;

        // 가상계좌 + 결제 완료 상태일 경우에만 환불 계좌 필요
        if (paymentEntity.getMethod().equals(METHOD.VIRTUAL_ACCOUNT) &&
                paymentEntity.getPaymentStatus().equals(PAYMENT_STATUS.DONE)) {
            bankCode = request.bankCode();
            accountNumber = request.accountNumber();
            holderName = request.holderName();
        }

        commonService.cancelTossPaymentApproval(
                paymentEntity.getPaymentKey(),
                request.cancelReason(),
                bankCode,
                accountNumber,
                holderName
        ).subscribe();

        paymentEntity.updateCANCELStatus(LocalDateTime.now());
        paymentEntityRepository.savePaymentEntity(paymentEntity);
    }

    @Transactional
    public void tossPaymentDepositCallBack(TossPaymentDepositCallBackRequest request) {
        PaymentEntity paymentEntity = paymentEntityRepository.findBySecret(request.secret())
                .orElseThrow(() -> new ServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "무통장 입금 전 결제가 저장되지 않았습니다.",
                        getCode("무통장 입금 전 결제가 저장되지 않았습니다.", ExceptionType.SERVER)
                ));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        LocalDateTime paymentCompletedAt = LocalDateTime.parse(request.createdAt(), formatter);

        switch (request.status()) {
            case "DONE" -> {
                paymentEntity.updateVirtualDONEStatus(paymentCompletedAt);
                paymentEntityRepository.savePaymentEntity(paymentEntity);
            }
            case "CANCELED", "PARTIAL_CANCELED" -> {
                paymentEntity.updateCANCELStatus(paymentCompletedAt);
                paymentEntityRepository.savePaymentEntity(paymentEntity);
            }
        }
    }

}
