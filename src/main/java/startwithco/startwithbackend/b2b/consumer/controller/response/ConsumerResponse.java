package startwithco.startwithbackend.b2b.consumer.controller.response;

import lombok.Builder;
import startwithco.startwithbackend.b2b.consumer.domain.ConsumerEntity;
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

    public record LoginConsumerResponse(
            String accessToken,
            String refreshToken,
            Long consumerSeq
    ) {}

    @Builder
    public record GetConsumerInfo(
            Long consumerSeq,
            String consumerName,
            String phoneNumber,
            String email,
            String industry,
            String consumerImageUrl
    ) {
        public static GetConsumerInfo fromEntity(ConsumerEntity consumerEntity) {
            return GetConsumerInfo.builder()
                    .consumerSeq(consumerEntity.getConsumerSeq())
                    .consumerName(consumerEntity.getConsumerName())
                    .phoneNumber(consumerEntity.getPhoneNumber())
                    .email(consumerEntity.getEmail())
                    .industry(consumerEntity.getIndustry())
                    .consumerImageUrl(consumerEntity.getConsumerImageUrl())
                    .build();
        }
    }
}
