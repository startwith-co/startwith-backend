package startwithco.startwithbackend.solution.keyword.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.solution.keyword.domain.SolutionKeywordEntity;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SolutionKeywordEntityRepositoryImpl implements SolutionKeywordEntityRepository {
    private final SolutionKeywordEntityJpaRepository repository;

    public List<SolutionKeywordEntity> saveAllSolutionKeywordEntities(List<SolutionKeywordEntity> solutionKeywordEntities) {
        return repository.saveAll(solutionKeywordEntities);
    }
}
