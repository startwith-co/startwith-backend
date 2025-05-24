package startwithco.startwithbackend.solution.solution.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.solution.solution.domain.SolutionEntity;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SolutionEntityRepositoryImpl implements SolutionEntityRepository {
    private final SolutionEntityJpaRepository repository;

    public SolutionEntity saveSolutionEntity(SolutionEntity solutionEntity) {
        return repository.save(solutionEntity);
    }

    @Override
    public Optional<SolutionEntity> findBySolutionSeq(Long solutionSeq) {
        return repository.findBySolutionSeq(solutionSeq);
    }
}
