package startwithco.startwithbackend.solution.erp.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.solution.solution.util.CATEGORY;
import startwithco.startwithbackend.solution.erp.domain.ErpEntity;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ErpEntityRepositoryImpl implements ErpEntityRepository {
    private final ErpEntityJpaRepository repository;

    @Override
    public Optional<ErpEntity> findByVendorSeqAndCategory(Long vendorSeq, CATEGORY category) {
        return repository.findByVendorSeqAndCategory(vendorSeq, category);
    }

    @Override
    public List<ErpEntity> findAllByVendorSeq(Long vendorSeq) {
        return repository.findAllByVendorSeq(vendorSeq);
    }
}
