package startwithco.startwithbackend.b2b.vendor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import startwithco.startwithbackend.b2b.consumer.domain.ConsumerEntity;
import startwithco.startwithbackend.b2b.vendor.controller.request.VendorRequest;
import startwithco.startwithbackend.b2b.vendor.controller.response.VendorResponse;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;
import startwithco.startwithbackend.b2b.vendor.repository.VendorEntityRepository;
import startwithco.startwithbackend.common.service.CommonService;
import startwithco.startwithbackend.common.util.CATEGORY;
import startwithco.startwithbackend.exception.conflict.ConflictErrorResult;
import startwithco.startwithbackend.exception.conflict.ConflictException;
import startwithco.startwithbackend.exception.notFound.NotFoundErrorResult;
import startwithco.startwithbackend.exception.notFound.NotFoundException;
import startwithco.startwithbackend.solution.erp.domain.ErpEntity;
import startwithco.startwithbackend.solution.erp.repository.ErpEntityRepository;
import startwithco.startwithbackend.solution.solution.repository.SolutionEntityRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static startwithco.startwithbackend.b2b.vendor.controller.response.VendorResponse.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorService {
    private final VendorEntityRepository vendorEntityRepository;
    private final ErpEntityRepository erpEntityRepository;
    private final CommonService commonService;
    private final BCryptPasswordEncoder encoder;

    @Transactional(readOnly = true)
    public List<GetVendorSolutionCategory> getVendorSolutionCategory(Long vendorSeq) {
        /*
         * [예외 처리]
         * 1. vendor 유효성 검사
         * */
        vendorEntityRepository.findByVendorSeq(vendorSeq)
                .orElseThrow(() -> new NotFoundException(NotFoundErrorResult.VENDOR_NOT_FOUND_EXCEPTION));
        List<ErpEntity> erpEntities = erpEntityRepository.findAllByVendorSeq(vendorSeq);

        List<GetVendorSolutionCategory> response = new ArrayList<>();
        for (ErpEntity erpEntity : erpEntities) {
            response.add(new GetVendorSolutionCategory(erpEntity.getCategory(), erpEntity.getSolutionSeq()));
        }

        return response;
    }

    @Transactional
    public void saveVendor(VendorRequest.SaveVendorRequest request, MultipartFile businessLicense) {
        vendorEntityRepository.findByEmail(request.email())
                .ifPresent(entity -> {
                    throw new ConflictException(ConflictErrorResult.VENDOR_EMAIL_DUPLICATION_CONFLICT_EXCEPTION);
                });


        try {
            String businessLicenseImage = commonService.uploadPDFFile(businessLicense);


            VendorEntity vendorEntity = VendorEntity.builder()
                    .vendorName(request.vendorName())
                    .managerName(request.managerName())
                    .phoneNumber(request.phoneNumber())
                    .email(request.email())
                    .encodedPassword(encoder.encode(request.password()))
                    .businessLicenseImage(businessLicenseImage)
                    .build();

            vendorEntityRepository.save(vendorEntity);


        } catch (DataIntegrityViolationException e) {
            log.error("Vendor Service saveVendor Method DataIntegrityViolationException-> {}", e.getMessage());

            throw new ConflictException(ConflictErrorResult.IDEMPOTENT_REQUEST_CONFLICT_EXCEPTION);
        } catch (IOException e) {
            log.error("Vendor Service saveVendor Method IOException-> {}", e.getMessage());

            throw new ConflictException(ConflictErrorResult.IDEMPOTENT_REQUEST_CONFLICT_EXCEPTION);
        }
    }
}
