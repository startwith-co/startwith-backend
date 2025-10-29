package startwithco.startwithbackend.solution.solution.repository;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.solution.solution.domain.SolutionEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolutionEntityJpaRepository extends JpaRepository<SolutionEntity, Long> {
    @Query("""
            SELECT s
            FROM SolutionEntity s
            WHERE s.vendorEntity.vendorSeq = :vendorSeq
              AND s.deleted is FALSE
            """)
    List<SolutionEntity> findAllByVendorSeq(@Param("vendorSeq") Long vendorSeq);

    @Query("""
            SELECT s
            FROM SolutionEntity s
            WHERE s.solutionSeq = :solutionSeq
              AND s.deleted is FALSE
            """)
    Optional<SolutionEntity> findBySolutionSeq(@Param("solutionSeq") Long solutionSeq);
}
