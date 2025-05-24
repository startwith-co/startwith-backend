package startwithco.startwithbackend.solution.erp.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.solution.erp.domain.ErpEntity;
import startwithco.startwithbackend.common.util.CATEGORY;

import java.util.List;
import java.util.Optional;

@Repository
public interface ErpEntityJpaRepository extends JpaRepository<ErpEntity, Long> {
    @Query("SELECT e FROM ErpEntity e WHERE e.vendorEntity.vendorSeq = :vendorSeq AND e.category = :category")
    Optional<ErpEntity> findByVendorSeqAndCategory(@Param("vendorSeq") long vendorSeq, @Param("category") CATEGORY category);

    @Query("SELECT e FROM ErpEntity e WHERE e.vendorEntity.vendorSeq = :vendorSeq")
    List<ErpEntity> findAllByVendorSeq(@Param("vendorSeq") long vendorSeq);
}
