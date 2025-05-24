package startwithco.startwithbackend.b2b.vendor.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;
import startwithco.startwithbackend.b2b.vendor.repository.VendorEntityRepository;
import startwithco.startwithbackend.common.util.CATEGORY;
import startwithco.startwithbackend.exception.notFound.NotFoundErrorResult;
import startwithco.startwithbackend.exception.notFound.NotFoundException;
import startwithco.startwithbackend.solution.erp.domain.ErpEntity;
import startwithco.startwithbackend.solution.erp.repository.ErpEntityRepository;
import startwithco.startwithbackend.solution.solution.repository.SolutionEntityRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VendorService {
    private final VendorEntityRepository vendorEntityRepository;
    private final ErpEntityRepository erpEntityRepository;

    public List<CATEGORY> getVendorSolutionCategory(Long vendorSeq) {
        /*
         * [예외 처리]
         * 1. vendor 유효성 검사
         * */
        vendorEntityRepository.findByVendorSeq(vendorSeq)
                .orElseThrow(() -> new NotFoundException(NotFoundErrorResult.VENDOR_NOT_FOUND_EXCEPTION));
        List<ErpEntity> erpEntities = erpEntityRepository.findAllByVendorSeq(vendorSeq);

        List<CATEGORY> categories = new ArrayList<>();
        for (ErpEntity erpEntity : erpEntities) {
            categories.add(erpEntity.getCategory());
        }

        return categories;
    }
}
