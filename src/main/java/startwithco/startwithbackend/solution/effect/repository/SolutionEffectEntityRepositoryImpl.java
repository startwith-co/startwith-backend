package startwithco.startwithbackend.solution.effect.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.solution.effect.domain.SolutionEffectEntity;
import startwithco.startwithbackend.solution.solution.domain.SolutionEntity;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SolutionEffectEntityRepositoryImpl implements SolutionEffectEntityRepository {
    private final SolutionEffectEntityJpaRepository repository;

    public List<SolutionEffectEntity> saveAll(List<SolutionEffectEntity> solutionEffectEntities) {
        return repository.saveAll(solutionEffectEntities);
    }

    @Override
    public void deleteAllBySolutionSeq(Long solutionSeq) {
        repository.deleteAllBySolutionSeq(solutionSeq);
    }
}
