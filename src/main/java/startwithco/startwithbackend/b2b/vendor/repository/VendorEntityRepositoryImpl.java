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

    @Override
    public Optional<VendorEntity> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Override
    public void save(VendorEntity vendorEntity) {
        repository.save(vendorEntity);
    }

    @Override
    public boolean existsByVendorSeq(Long vendorSeq) {
        return repository.existsByVendorSeq(vendorSeq);
    }
}
