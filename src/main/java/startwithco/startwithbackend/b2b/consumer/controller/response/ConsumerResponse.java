package startwithco.startwithbackend.b2b.consumer.controller.response;

import lombok.Builder;
import startwithco.startwithbackend.b2b.consumer.domain.ConsumerEntity;
import startwithco.startwithbackend.payment.payment.util.METHOD;
import startwithco.startwithbackend.payment.payment.util.PAYMENT_STATUS;
import startwithco.startwithbackend.solution.solution.util.CATEGORY;

import java.time.LocalDateTime;

public class ConsumerResponse {
    public record LoginConsumerResponse(
            String accessToken,
            String refreshToken,
            Long consumerSeq,
            String consumerUniqueType,
            String consumerName
    ) {
    }

    @Builder
    public record GetConsumerInfo(
            Long consumerSeq,
            String consumerName,
            String phoneNumber,
            String email,
            String industry,
            String consumerImageUrl,
            String consumerUniqueType
    ) {
        public static GetConsumerInfo fromEntity(ConsumerEntity consumerEntity) {
            return GetConsumerInfo.builder()
                    .consumerSeq(consumerEntity.getConsumerSeq())
                    .consumerName(consumerEntity.getConsumerName())
                    .phoneNumber(consumerEntity.getPhoneNumber())
                    .email(consumerEntity.getEmail())
                    .industry(consumerEntity.getIndustry())
                    .consumerImageUrl(consumerEntity.getConsumerImageUrl())
                    .consumerUniqueType(consumerEntity.getConsumerUniqueType())
                    .build();
        }
    }

    public record GetConsumerDashboardResponse(
            Long consumerSeq,
            Long vendorSeq,
            PAYMENT_STATUS paymentStatus,
            LocalDateTime paymentCompletedAt,
            String representImageUrl,
            String vendorName,
            String vendorUniqueType,
            Long solutionSeq,
            String solutionName,
            METHOD method,
            Long amount,
            boolean existReview,
            CATEGORY category
    ) {

    }

    public record ResetLinkResponse(
            String token,
            String link,
            Long consumerSeq
    ) {

    }
}
