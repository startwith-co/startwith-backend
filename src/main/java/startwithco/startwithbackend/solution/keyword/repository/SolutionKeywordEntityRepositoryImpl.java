package startwithco.startwithbackend.solution.keyword.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.solution.keyword.domain.SolutionKeywordEntity;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SolutionKeywordEntityRepositoryImpl implements SolutionKeywordEntityRepository {
    private final SolutionKeywordEntityJpaRepository repository;

    public List<SolutionKeywordEntity> saveAll(List<SolutionKeywordEntity> solutionKeywordEntities) {
        return repository.saveAll(solutionKeywordEntities);
    }

    @Override
    public void deleteAllBySolutionSeq(Long solutionSeq) {
        repository.deleteAllBySolutionSeq(solutionSeq);
    }

    @Override
    public List<String> findAllKeywordsBySolutionSeq(Long solutionSeq) {
        return repository.findAllKeywordsBySolutionSeq(solutionSeq);
    }
}
