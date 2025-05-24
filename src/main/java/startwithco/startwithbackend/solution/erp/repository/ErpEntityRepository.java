package startwithco.startwithbackend.solution.erp.repository;

import startwithco.startwithbackend.solution.erp.domain.ErpEntity;
import startwithco.startwithbackend.common.util.CATEGORY;

import java.util.List;
import java.util.Optional;

public interface ErpEntityRepository {
    Optional<ErpEntity> findByVendorSeqAndCategory(Long vendorSeq, CATEGORY category);

    List<ErpEntity> findAllByVendorSeq(Long vendorSeq);
}
