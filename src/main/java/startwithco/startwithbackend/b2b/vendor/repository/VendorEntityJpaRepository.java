package startwithco.startwithbackend.b2b.vendor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;

import java.util.Optional;

@Repository
public interface VendorEntityJpaRepository extends JpaRepository<VendorEntity, Long> {
    @Query("""
            SELECT v FROM VendorEntity v
            WHERE v.vendorSeq = :vendorSeq
            """)
    Optional<VendorEntity> findByVendorSeq(Long vendorSeq);

    Optional<VendorEntity> findByEmail(String email);

    @Query("""
            SELECT CASE
                    WHEN COUNT(v) > 0 THEN true
                    ELSE false
                  END
            FROM VendorEntity v
            WHERE v.vendorSeq = :vendorSeq
            """)
    boolean existsByVendorSeq(Long vendorSeq);
}