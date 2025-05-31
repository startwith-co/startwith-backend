package startwithco.startwithbackend.solution.review.controller.response;

import java.time.LocalDateTime;

public class SolutionReviewResponse {
    public record SaveSolutionReviewResponse(
            Long solutionReviewSeq
    ) {

    }

    public record GetAllSolutionReviewResponse(
            Long consumerSeq,
            String consumerName,
            String consumerImageUrl,
            Double start,
            String comment,
            LocalDateTime createdAt
    ) {

    }
}
