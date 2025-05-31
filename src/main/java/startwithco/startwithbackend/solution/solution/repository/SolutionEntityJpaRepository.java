package startwithco.startwithbackend.solution.solution.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.solution.solution.domain.SolutionEntity;
import startwithco.startwithbackend.solution.solution.util.CATEGORY;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolutionEntityJpaRepository extends JpaRepository<SolutionEntity, Long> {
    @Query("SELECT s FROM SolutionEntity s WHERE s.vendorEntity.vendorSeq = :vendorSeq AND s.category = :category")
    Optional<SolutionEntity> findByVendorSeqAndCategory(@Param("vendorSeq") Long vendorSeq, @Param("category") CATEGORY category);

    @Query("SELECT s FROM SolutionEntity s WHERE s.vendorEntity.vendorSeq = :vendorSeq")
    List<SolutionEntity> findAllByVendorSeq(@Param("vendorSeq") Long vendorSeq);

    @Query("""
            SELECT s
            FROM SolutionEntity s
            WHERE s.solutionSeq = :solutionSeq
            """)
    Optional<SolutionEntity> findBySolutionSeq(@Param("solutionSeq") Long solutionSeq);
}
