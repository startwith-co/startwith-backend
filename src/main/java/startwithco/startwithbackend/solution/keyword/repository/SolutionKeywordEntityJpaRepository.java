package startwithco.startwithbackend.solution.keyword.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import startwithco.startwithbackend.solution.keyword.domain.SolutionKeywordEntity;

@Repository
public interface SolutionKeywordEntityJpaRepository extends JpaRepository<SolutionKeywordEntity, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM SolutionKeywordEntity sk WHERE sk.solutionEntity.solutionSeq = :solutionSeq")
    void deleteAllBySolutionSeq(@Param("solutionSeq") Long solutionSeq);
}
