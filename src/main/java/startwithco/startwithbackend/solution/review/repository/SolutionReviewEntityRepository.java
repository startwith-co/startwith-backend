package startwithco.startwithbackend.solution.review.repository;

import startwithco.startwithbackend.solution.review.domain.SolutionReviewEntity;

import java.util.List;
import java.util.Optional;

import static startwithco.startwithbackend.solution.review.controller.response.SolutionReviewResponse.*;

public interface SolutionReviewEntityRepository {
    SolutionReviewEntity saveSolutionReviewEntity(SolutionReviewEntity solutionReviewEntity);

    Optional<SolutionReviewEntity> findBySolutionSeqAndConsumerSeqAndSolutionReviewSeq(Long solutionSeq, Long consumerSeq, Long solutionReviewSeq);

    List<GetAllSolutionReviewResponse> findAllBySolutionSeq(Long solutionSeq);

    Long countBySolutionSeq(Long solutionSeq);

    Double averageBySolutionSeq(Long solutionSeq);

    boolean existsByConsumerSeqAndSolutionSeq(Long consumerSeq, Long solutionSeq);
}
