package startwithco.startwithbackend.solution.effect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import startwithco.startwithbackend.solution.effect.domain.SolutionEffectEntity;

import java.util.List;

@Repository
public interface SolutionEffectEntityJpaRepository extends JpaRepository<SolutionEffectEntity, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM SolutionEffectEntity se WHERE se.solutionEntity.solutionSeq = :solutionSeq")
    void deleteAllBySolutionSeq(Long solutionSeq);

    @Query("""
            SELECT see
            FROM SolutionEffectEntity see
            WHERE see.solutionEntity.solutionSeq = :solutionSeq
            """)
    List<SolutionEffectEntity> findAllBySolutionSeq(Long solutionSeq);
}
