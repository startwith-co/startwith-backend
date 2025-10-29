package startwithco.startwithbackend.solution.review.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.b2b.consumer.domain.QConsumerEntity;
import startwithco.startwithbackend.solution.review.domain.QSolutionReviewEntity;
import startwithco.startwithbackend.solution.review.domain.SolutionReviewEntity;

import java.util.List;
import java.util.Optional;

import static startwithco.startwithbackend.solution.review.controller.response.SolutionReviewResponse.*;

@Repository
@RequiredArgsConstructor
public class SolutionReviewEntityRepositoryImpl implements SolutionReviewEntityRepository {
    private final SolutionReviewEntityJpaRepository repository;
    private final JPAQueryFactory queryFactory;

    @Override
    public SolutionReviewEntity saveSolutionReviewEntity(SolutionReviewEntity solutionReviewEntity) {
        return repository.save(solutionReviewEntity);
    }

    @Override
    public Optional<SolutionReviewEntity> findBySolutionSeqAndConsumerSeqAndSolutionReviewSeq(Long solutionSeq, Long consumerSeq, Long solutionReviewSeq) {
        return repository.findBySolutionSeqAndConsumerSeqAndSolutionReviewSeq(solutionSeq, consumerSeq, solutionReviewSeq);
    }

    @Override
    public List<GetAllSolutionReviewResponse> findAllBySolutionSeq(Long solutionSeq) {
        QConsumerEntity qConsumerEntity = QConsumerEntity.consumerEntity;
        QSolutionReviewEntity qSolutionReviewEntity = QSolutionReviewEntity.solutionReviewEntity;

        return queryFactory
                .select(Projections.constructor(
                        GetAllSolutionReviewResponse.class,
                        qConsumerEntity.consumerSeq,
                        qConsumerEntity.consumerName,
                        qConsumerEntity.consumerImageUrl,
                        qSolutionReviewEntity.star,
                        qSolutionReviewEntity.comment,
                        qSolutionReviewEntity.createdAt
                ))
                .from(qSolutionReviewEntity)
                .innerJoin(qSolutionReviewEntity.consumerEntity, qConsumerEntity)
                .where(qSolutionReviewEntity.solutionEntity.solutionSeq.eq(solutionSeq))
                .orderBy(qSolutionReviewEntity.createdAt.desc())
                .fetch();
    }

    @Override
    public Long countBySolutionSeq(Long solutionSeq) {
        return repository.countBySolutionSeq(solutionSeq);
    }

    @Override
    public Double averageBySolutionSeq(Long solutionSeq) {
        return repository.averageBySolutionSeq(solutionSeq);
    }

    @Override
    public boolean existsByConsumerSeqAndSolutionSeq(Long consumerSeq, Long solutionSeq) {
        return repository.existsByConsumerSeqAndSolutionSeq(consumerSeq, solutionSeq);
    }
}
