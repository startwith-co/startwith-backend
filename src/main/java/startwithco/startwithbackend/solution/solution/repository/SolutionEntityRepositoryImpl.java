package startwithco.startwithbackend.solution.solution.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.exception.BadRequestException;
import startwithco.startwithbackend.exception.code.ExceptionCodeMapper;
import startwithco.startwithbackend.solution.keyword.domain.QSolutionKeywordEntity;
import startwithco.startwithbackend.solution.solution.domain.QSolutionEntity;
import startwithco.startwithbackend.solution.solution.domain.SolutionEntity;
import startwithco.startwithbackend.solution.solution.util.CATEGORY;

import java.util.List;
import java.util.Optional;

import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.getCode;

@Repository
@RequiredArgsConstructor
public class SolutionEntityRepositoryImpl implements SolutionEntityRepository {
    private final SolutionEntityJpaRepository repository;
    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

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

    @Override
    public List<SolutionEntity> findByCategoryAndIndustryAndBudgetAndKeyword(CATEGORY category, String industry, String budget, String keyword, int start, int end) {
        QSolutionEntity qSolutionEntity = QSolutionEntity.solutionEntity;
        QSolutionKeywordEntity qSolutionKeywordEntity = QSolutionKeywordEntity.solutionKeywordEntity;

        BooleanBuilder builder = new BooleanBuilder();
        if (category != null) {
            builder.and(qSolutionEntity.category.eq(category));
        }

        if (industry != null) {
            builder.andAnyOf(
                    qSolutionEntity.industry.eq(industry),
                    qSolutionEntity.industry.like(industry + ",%"),
                    qSolutionEntity.industry.like("%," + industry),
                    qSolutionEntity.industry.like("%," + industry + ",%")
            );
        }

        if (!budget.equals("전체")) {
            switch (budget) {
                case "500,000원 미만" -> builder.and(qSolutionEntity.amount.lt(500_000L));
                case "500,000원~1,000,000원 미만" ->
                        builder.and(qSolutionEntity.amount.goe(500_000L).and(qSolutionEntity.amount.lt(1_000_000L)));
                case "1,000,000원~3,000,000원 미만" ->
                        builder.and(qSolutionEntity.amount.goe(1_000_000L).and(qSolutionEntity.amount.lt(3_000_000L)));
                case "3,000,000원~5,000,000원 미만" ->
                        builder.and(qSolutionEntity.amount.goe(3_000_000L).and(qSolutionEntity.amount.lt(5_000_000L)));
                case "5,000,000원~10,000,000원 미만" ->
                        builder.and(qSolutionEntity.amount.goe(5_000_000L).and(qSolutionEntity.amount.lt(10_000_000L)));
                case "10,000,000원 이상" -> builder.and(qSolutionEntity.amount.goe(10_000_000L));
                default -> throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
                );
            }
        }

        if (keyword != null && !keyword.isBlank()) {
            return queryFactory
                    .select(qSolutionEntity)
                    .from(qSolutionKeywordEntity)
                    .join(qSolutionKeywordEntity.solutionEntity, qSolutionEntity)
                    .join(qSolutionEntity.vendorEntity).fetchJoin()
                    .where(builder)
                    .offset(start)
                    .limit(end - start)
                    .fetch();
        } else {
            return queryFactory
                    .select(qSolutionEntity)
                    .from(qSolutionEntity)
                    .join(qSolutionEntity.vendorEntity).fetchJoin()
                    .where(builder)
                    .offset(start)
                    .limit(end - start)
                    .fetch();
        }
    }
}
