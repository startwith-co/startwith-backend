package startwithco.startwithbackend.solution.keyword.repository;

import startwithco.startwithbackend.solution.keyword.domain.SolutionKeywordEntity;

import java.util.List;

public interface SolutionKeywordEntityRepository {
    List<SolutionKeywordEntity> saveAll(List<SolutionKeywordEntity> solutionKeywordEntities);

    void deleteAllBySolutionSeq(Long solutionSeq);
}
