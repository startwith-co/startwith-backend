package startwithco.startwithbackend.payment.paymentEvent.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
            CATEGORY category,
            Long amount,
            SELL_TYPE sellType,
            Long duration,
            PAYMENT_EVENT_STATUS paymentEventStatus,

            // 아래는 상태에 따라 null일 수 있음
            Long actualDuration,
            LocalDateTime paymentCompletedAt,
            LocalDateTime developmentCompletedAt,
            LocalDateTime autoConfirmScheduledAt
    ) {

    }
}
