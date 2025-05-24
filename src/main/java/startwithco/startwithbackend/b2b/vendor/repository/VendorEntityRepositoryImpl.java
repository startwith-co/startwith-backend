package startwithco.startwithbackend.b2b.vendor.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VendorEntityRepositoryImpl implements VendorEntityRepository {
    private final VendorEntityJpaRepository repository;

    public Optional<VendorEntity> findByVendorSeq(Long vendorSeq) {
        return repository.findByVendorSeq(vendorSeq);
    }
}
