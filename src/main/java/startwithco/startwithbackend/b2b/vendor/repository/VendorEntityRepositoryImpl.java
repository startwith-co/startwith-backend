package startwithco.startwithbackend.b2b.vendor.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;
import startwithco.startwithbackend.exception.notFound.NotFoundErrorResult;
import startwithco.startwithbackend.exception.notFound.NotFoundException;

@Repository
@RequiredArgsConstructor
public class VendorEntityRepositoryImpl implements VendorEntityRepository {
    private final VendorEntityJpaRepository repository;

    public VendorEntity findByVendorSeq(Long vendorSeq) {
        return repository.findByVendorSeq(vendorSeq)
                .orElseThrow(() -> new NotFoundException(NotFoundErrorResult.VENDOR_NOT_FOUND_EXCEPTION));
    }
}
