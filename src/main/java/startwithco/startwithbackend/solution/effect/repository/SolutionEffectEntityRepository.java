package startwithco.startwithbackend.solution.effect.repository;

import startwithco.startwithbackend.solution.effect.domain.SolutionEffectEntity;

import java.util.List;

public interface SolutionEffectEntityRepository {
    List<SolutionEffectEntity> saveAll(List<SolutionEffectEntity> solutionEffectEntities);

    void deleteAllBySolutionSeq(Long solutionSeq);
}
