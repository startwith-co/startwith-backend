package startwithco.startwithbackend.solution.review.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.solution.review.domain.SolutionReviewEntity;

import java.util.Optional;

@Repository
public interface SolutionReviewEntityJpaRepository extends JpaRepository<SolutionReviewEntity, Long> {
    @Query("""
            SELECT sre
            FROM SolutionReviewEntity sre
            WHERE sre.solutionEntity.solutionSeq = :solutionSeq
              AND sre.consumerEntity.consumerSeq = :consumerSeq
              AND sre.solutionReviewSeq = :solutionReviewSeq
            """)
    Optional<SolutionReviewEntity> findBySolutionSeqAndConsumerSeqAndSolutionReviewSeq(
            @Param("solutionSeq") Long solutionSeq,
            @Param("consumerSeq") Long consumerSeq,
            @Param("solutionReviewSeq") Long solutionReviewSeq
    );

    @Query("""
            SELECT COUNT(sre)
            FROM SolutionReviewEntity sre
            WHERE sre.solutionEntity.solutionSeq = :solutionSeq
            """)
    Long countBySolutionSeq(@Param("solutionSeq") Long solutionSeq);

    @Query("""
            SELECT AVG(sre.star)
            FROM SolutionReviewEntity sre
            WHERE sre.solutionEntity.solutionSeq = :solutionSeq
            """)
    Double averageBySolutionSeq(@Param("solutionSeq") Long solutionSeq);
}
