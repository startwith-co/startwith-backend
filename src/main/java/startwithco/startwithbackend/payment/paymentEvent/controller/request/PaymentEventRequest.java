package startwithco.startwithbackend.payment.paymentEvent.controller.request;

import startwithco.startwithbackend.payment.paymentEvent.util.PAYMENT_EVENT_ROUND;
import startwithco.startwithbackend.solution.solution.util.SELL_TYPE;
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
            Long duration,
            String paymentEventRound
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

            if (amount < 0) {
                throw new BadRequestException(BadRequestErrorResult.BAD_REQUEST_EXCEPTION);
            }

            if (sellType.equals("SINGLE")) {
                if (isBlank(paymentEventRound)) {
                    throw new BadRequestException(BadRequestErrorResult.BAD_REQUEST_EXCEPTION);
                }

                try {
                    SELL_TYPE.valueOf(sellType.toUpperCase());
                    PAYMENT_EVENT_ROUND.valueOf(paymentEventRound.toUpperCase());
                } catch (Exception e) {
                    throw new BadRequestException(BadRequestErrorResult.BAD_REQUEST_EXCEPTION);
                }
            } else if (sellType.equals("SUBSCRIBE")) {
                if (!isBlank(paymentEventRound)) {
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

    public record DeletePaymentEventRequest(
            Long paymentEventSeq
    ) {
        public void validate() {
            if (paymentEventSeq == null) {
                throw new BadRequestException(BadRequestErrorResult.BAD_REQUEST_EXCEPTION);
            }
        }
    }
}
