package startwithco.startwithbackend.solution.solution.repository;

import startwithco.startwithbackend.solution.solution.domain.SolutionEntity;

import java.util.Optional;

public interface SolutionEntityRepository {
    SolutionEntity saveSolutionEntity(SolutionEntity solutionEntity);

    Optional<SolutionEntity> findBySolutionSeq(Long solutionSeq);
}
