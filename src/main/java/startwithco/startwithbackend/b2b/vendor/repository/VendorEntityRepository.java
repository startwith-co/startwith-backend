package startwithco.startwithbackend.b2b.vendor.repository;

import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;

import java.util.Optional;

public interface VendorEntityRepository {
    Optional<VendorEntity> findByVendorSeq(Long vendorSeq);

    Optional<VendorEntity> findByEmail(String email);

    void save(VendorEntity vendorEntity);
}
