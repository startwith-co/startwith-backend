package startwithco.startwithbackend.payment.payment.controller.request;

import startwithco.startwithbackend.exception.badRequest.BadRequestErrorResult;
import startwithco.startwithbackend.exception.badRequest.BadRequestException;

import static io.micrometer.common.util.StringUtils.isBlank;

public class PaymentRequest {
    public record TossPaymentApprovalRequest(
            Long paymentEventSeq,
            String paymentKey,
            String orderId,
            Long amount
    ) {
        public void validate() {
            if (paymentEventSeq == null || isBlank(paymentKey) || isBlank(orderId) || amount <= 0) {
                throw new BadRequestException(BadRequestErrorResult.BAD_REQUEST_EXCEPTION);
            }
        }
    }
}
