package startwithco.startwithbackend.b2b.stat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.b2b.stat.domain.StatEntity;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;

import java.util.List;

@Repository
public interface StatEntityJpaRepository extends JpaRepository<StatEntity, Long> {
    void deleteAllByVendor(VendorEntity vendor);

    List<StatEntity> findAllByVendor(VendorEntity vendor);
}
