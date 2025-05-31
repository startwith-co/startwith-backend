package startwithco.startwithbackend.b2b.consumer.controller.response;

import startwithco.startwithbackend.payment.payment.util.METHOD;
import startwithco.startwithbackend.payment.paymentEvent.util.PAYMENT_EVENT_STATUS;

import java.time.LocalDateTime;

public class ConsumerResponse {
    public record GetConsumerDashboardResponse(
            PAYMENT_EVENT_STATUS paymentEventStatus,

            Long solutionSeq,
            String solutionName,
            String representImageUrl,
            Boolean existSolutionReview,

            Long vendorSeq,
            String vendorName,

            LocalDateTime paymentCompletedAt,
            METHOD method,
            Long amount
    ) {

    }
}
