package startwithco.startwithbackend.b2b.consumer.controller.response;

import lombok.Builder;
import startwithco.startwithbackend.b2b.consumer.domain.ConsumerEntity;

public class ConsumerResponse {
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
