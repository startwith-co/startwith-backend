package startwithco.startwithbackend.b2b.vendor.repository;

import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;

public interface VendorEntityRepository {
    VendorEntity findByVendorSeq(Long vendorSeq);
}
