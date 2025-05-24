package startwithco.startwithbackend.payment.paymentEvent.controller.request;

import startwithco.startwithbackend.common.util.SELL_TYPE;
import startwithco.startwithbackend.exception.badRequest.BadRequestErrorResult;
import startwithco.startwithbackend.exception.badRequest.BadRequestException;

import static io.micrometer.common.util.StringUtils.isBlank;

public class PaymentEventRequest {
    public record SavePaymentEventRequest(
            Long solutionSeq,
            Long consumerSeq,
            Long vendorSeq,
            String paymentEventName,
            String sellType,
            Long amount,
            Long duration
    ) {
        public void validate() {
            if (solutionSeq == null ||
                    consumerSeq == null ||
                    vendorSeq == null ||
                    isBlank(paymentEventName) ||
                    isBlank(sellType) ||
                    amount == null ||
                    duration == null) {
                throw new BadRequestException(BadRequestErrorResult.BAD_REQUEST_EXCEPTION);
            }

            try {
                SELL_TYPE.valueOf(sellType.toUpperCase());
            } catch (Exception e) {
                throw new BadRequestException(BadRequestErrorResult.BAD_REQUEST_EXCEPTION);
            }
        }
    }
}
