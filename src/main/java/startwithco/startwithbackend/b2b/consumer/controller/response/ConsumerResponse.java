package startwithco.startwithbackend.b2b.consumer.controller.response;

public class ConsumerResponse {
    public record ConsumerDetailResponse(
            Long DEVELOPING,
            Long DEVELOPED,
            Long CONFIRMED
    ) {

    }
}
