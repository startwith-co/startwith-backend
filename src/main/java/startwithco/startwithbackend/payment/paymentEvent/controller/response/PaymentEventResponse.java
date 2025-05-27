package startwithco.startwithbackend.payment.paymentEvent.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import startwithco.startwithbackend.payment.paymentEvent.util.PAYMENT_EVENT_ROUND;
import startwithco.startwithbackend.payment.paymentEvent.util.PAYMENT_EVENT_STATUS;
import startwithco.startwithbackend.solution.solution.util.CATEGORY;
import startwithco.startwithbackend.solution.solution.util.SELL_TYPE;

import java.time.LocalDateTime;

public class PaymentEventResponse {
    public record SavePaymentEventEntityResponse(
            Long paymentEventSeq
    ) {

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record GetPaymentEventEntityResponse(
            Long paymentEventSeq,
            String paymentEventName,
            SELL_TYPE sellType,
            Long amount,
            Long duration,
            PAYMENT_EVENT_ROUND paymentEventRound,
            PAYMENT_EVENT_STATUS paymentEventStatus
    ) {

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record GetPaymentEventEntityOrderResponse(
            String representImageUrl,
            String paymentEventName,
            String vendorBannerImageUrl,
            String vendorName,
            CATEGORY category,
            Long duration,
            Long amount,
            Long actualAmount
    ) {

    }
}
