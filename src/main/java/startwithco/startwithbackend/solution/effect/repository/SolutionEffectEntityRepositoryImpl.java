package startwithco.startwithbackend.solution.effect.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.solution.effect.domain.QSolutionEffectEntity;
import startwithco.startwithbackend.solution.effect.domain.SolutionEffectEntity;

import java.util.List;

import static startwithco.startwithbackend.solution.solution.controller.response.SolutionResponse.GetSolutionEntityResponse.*;

@Repository
@RequiredArgsConstructor
public class SolutionEffectEntityRepositoryImpl implements SolutionEffectEntityRepository {
    private final SolutionEffectEntityJpaRepository repository;
    private final JPAQueryFactory queryFactory;

    @Override
    public List<SolutionEffectEntity> saveAll(List<SolutionEffectEntity> solutionEffectEntities) {
        return repository.saveAll(solutionEffectEntities);
    }

    @Override
    public void deleteAllBySolutionSeq(Long solutionSeq) {
        repository.deleteAllBySolutionSeq(solutionSeq);
    }

    @Override
    public List<SolutionEffectResponse> findAllBySolutionSeqCustom(Long solutionSeq) {
        QSolutionEffectEntity qSolutionEffectEntity = QSolutionEffectEntity.solutionEffectEntity;

        return queryFactory
                .select(Projections.constructor(
                        SolutionEffectResponse.class,
                        qSolutionEffectEntity.effectName,
                        qSolutionEffectEntity.percent,
                        qSolutionEffectEntity.direction
                ))
                .from(qSolutionEffectEntity)
                .where(qSolutionEffectEntity.solutionEntity.solutionSeq.eq(solutionSeq))
                .orderBy(qSolutionEffectEntity.solutionEffectSeq.asc())
                .fetch();
    }
}
