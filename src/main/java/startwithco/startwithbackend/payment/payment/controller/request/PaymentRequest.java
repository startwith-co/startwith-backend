package startwithco.startwithbackend.payment.payment.controller.request;

import org.springframework.http.HttpStatus;
import startwithco.startwithbackend.exception.BadRequestException;
import startwithco.startwithbackend.exception.code.ExceptionCodeMapper;

import static io.micrometer.common.util.StringUtils.isBlank;
import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.getCode;

public class PaymentRequest {
    public record TossPaymentApprovalRequest(
            Long paymentEventSeq,
            String paymentKey,
            String orderId,
            Long amount
    ) {
        public void validate() {
            if (paymentEventSeq == null || isBlank(paymentKey) || isBlank(orderId) || amount <= 0) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
                );
            }
        }
    }

    public record TossPaymentDepositCallBackRequest(
            String createdAt,
            String secret,
            String status,
            String transactionKey,
            String orderId
    ) {

    }

    public record RefundTossPaymentApprovalRequest(
            String orderId,
            String cancelReason,
            String bankCode,
            String accountNumber,
            String holderName
    ) {
        public void validate() {
            if (orderId == null || orderId.isEmpty()) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
                );
            }

            boolean hasAnyRefundAccountField =
                    bankCode != null || accountNumber != null || holderName != null;

            boolean hasAllRefundAccountFields =
                    bankCode != null && accountNumber != null && holderName != null;

            if (hasAnyRefundAccountField && !hasAllRefundAccountFields) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
                );
            }
        }
    }
}
