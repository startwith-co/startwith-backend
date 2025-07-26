package startwithco.startwithbackend.b2b.vendor.repository;

import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;

import java.util.List;
import java.util.Optional;

import static startwithco.startwithbackend.b2b.vendor.controller.response.VendorResponse.*;

public interface VendorEntityRepository {
    Optional<VendorEntity> findByVendorSeq(Long vendorSeq);

    Optional<VendorEntity> findByEmail(String email);

    void save(VendorEntity vendorEntity);

    boolean existsByVendorSeq(Long vendorSeq);

    void saveBlackToken(String token);

    List<VendorEntity> getAllVendorEntity(int start, int end);
}
