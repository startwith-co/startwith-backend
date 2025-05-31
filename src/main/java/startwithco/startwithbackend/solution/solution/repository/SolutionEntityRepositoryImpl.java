package startwithco.startwithbackend.solution.solution.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.solution.solution.domain.QSolutionEntity;
import startwithco.startwithbackend.solution.solution.domain.SolutionEntity;
import startwithco.startwithbackend.solution.solution.util.CATEGORY;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SolutionEntityRepositoryImpl implements SolutionEntityRepository {
    private final SolutionEntityJpaRepository repository;
    private final JPAQueryFactory queryFactory;

    public SolutionEntity saveSolutionEntity(SolutionEntity solutionEntity) {
        return repository.save(solutionEntity);
    }

    @Override
    public Optional<SolutionEntity> findByVendorSeqAndCategory(Long vendorSeq, CATEGORY category) {
        QSolutionEntity qSolutionEntity = QSolutionEntity.solutionEntity;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qSolutionEntity.vendorEntity.vendorSeq.eq(vendorSeq));
        if (category != null) {
            builder.and(qSolutionEntity.category.eq(category));
        }

        return Optional.ofNullable(
                queryFactory
                        .selectFrom(qSolutionEntity)
                        .where(builder)
                        .fetchOne()
        );
    }

    @Override
    public List<SolutionEntity> findAllByVendorSeq(Long vendorSeq) {
        return repository.findAllByVendorSeq(vendorSeq);
    }

    @Override
    public Optional<SolutionEntity> findBySolutionSeq(Long solutionSeq) {
        return repository.findBySolutionSeq(solutionSeq);
    }
}
