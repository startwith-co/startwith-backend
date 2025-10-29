package startwithco.startwithbackend.solution.keyword.repository;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import startwithco.startwithbackend.solution.keyword.domain.SolutionKeywordEntity;

import java.util.List;

@Repository
public interface SolutionKeywordEntityJpaRepository extends JpaRepository<SolutionKeywordEntity, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM SolutionKeywordEntity sk WHERE sk.solutionEntity.solutionSeq = :solutionSeq")
    void deleteAllBySolutionSeq(@Param("solutionSeq") Long solutionSeq);

    @Query("""
           SELECT ske.keyword
           FROM SolutionKeywordEntity ske
           WHERE ske.solutionEntity.solutionSeq = :solutionSeq
           ORDER BY ske.solutionKeywordSeq ASC
           """)
    List<String> findAllKeywordsBySolutionSeq(@Param("solutionSeq") Long solutionSeq);
}
