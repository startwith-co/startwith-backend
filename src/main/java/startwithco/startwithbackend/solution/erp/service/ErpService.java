package startwithco.startwithbackend.solution.erp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;
import startwithco.startwithbackend.b2b.vendor.repository.VendorEntityRepository;
import startwithco.startwithbackend.common.util.CATEGORY;
import startwithco.startwithbackend.common.util.DIRECTION;
import startwithco.startwithbackend.common.util.SELL_TYPE;
import startwithco.startwithbackend.exception.conflict.ConflictErrorResult;
import startwithco.startwithbackend.exception.conflict.ConflictException;
import startwithco.startwithbackend.exception.notFound.NotFoundErrorResult;
import startwithco.startwithbackend.exception.notFound.NotFoundException;
import startwithco.startwithbackend.exception.server.ServerErrorResult;
import startwithco.startwithbackend.exception.server.ServerException;
import startwithco.startwithbackend.solution.effect.domain.SolutionEffectEntity;
import startwithco.startwithbackend.solution.effect.repository.SolutionEffectEntityRepository;
import startwithco.startwithbackend.solution.erp.domain.ErpEntity;
import startwithco.startwithbackend.solution.erp.repository.ErpEntityRepository;
import startwithco.startwithbackend.solution.keyword.domain.SolutionKeywordEntity;
import startwithco.startwithbackend.solution.keyword.repository.SolutionKeywordEntityRepository;
import startwithco.startwithbackend.solution.solution.domain.SolutionEntity;
import startwithco.startwithbackend.solution.solution.repository.SolutionEntityRepository;
import startwithco.startwithbackend.common.service.CommonService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static startwithco.startwithbackend.solution.erp.controller.request.ErpRequest.*;
import static startwithco.startwithbackend.solution.erp.controller.response.ErpResponse.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ErpService {
    private final VendorEntityRepository vendorEntityRepository;
    private final ErpEntityRepository erpEntityRepository;
    private final SolutionEntityRepository solutionEntityRepository;
    private final SolutionEffectEntityRepository solutionEffectEntityRepository;
    private final SolutionKeywordEntityRepository solutionKeywordEntityRepository;

    private final CommonService commonService;

    @Transactional
    public SaveErpEntityResponse saveErpEntity(SaveErpEntityRequest request, MultipartFile representImageUrl, MultipartFile descriptionPdfUrl) {
        /*
         * [예외 처리]
         * 1. vendor 유효성 검사
         * 2. Solution 존재 여부 확인
         * 3. 멱등성 오류
         * 4. 서버 오류
         * */
        VendorEntity vendorEntity = vendorEntityRepository.findByVendorSeq(request.vendorSeq())
                .orElseThrow(() -> new NotFoundException(NotFoundErrorResult.VENDOR_NOT_FOUND_EXCEPTION));
        erpEntityRepository.findByVendorSeqAndCategory(request.vendorSeq(), CATEGORY.valueOf(request.category()))
                .ifPresent(solutionEntity -> {
                    throw new ConflictException(ConflictErrorResult.SOLUTION_CONFLICT_EXCEPTION);
                });

        try {
            // 1. ErpEntity -> SolutionEntity 저장
            String S3RepresentImageUrl = commonService.uploadJPGFile(representImageUrl);
            String S3DescriptionPdfUrl = commonService.uploadPDFFile(descriptionPdfUrl);

            ErpEntity erpEntity = ErpEntity.builder()
                    .vendorEntity(vendorEntity)
                    .solutionName(request.solutionName())
                    .solutionDetail(request.solutionDetail())
                    .category(CATEGORY.valueOf(request.category()))
                    .industry(request.industry())
                    .recommendedCompanySize(request.recommendedCompanySize())
                    .solutionImplementationType(request.solutionImplementationType())
                    .amount(request.amount())
                    .sellType(SELL_TYPE.valueOf(request.sellType()))
                    .duration(request.duration())
                    .specialist(request.specialist())
                    .representImageUrl(S3RepresentImageUrl)
                    .descriptionPdfUrl(S3DescriptionPdfUrl)
                    .build();

            SolutionEntity solutionEntity = solutionEntityRepository.saveSolutionEntity(erpEntity);

            // 2. SolutionEffectEntity 저장
            List<SolutionEffectEntity> solutionEffectEntities = Optional.ofNullable(request.solutionEffect())
                    .orElse(List.of())
                    .stream()
                    .map(e -> SolutionEffectEntity.builder()
                            .solutionEntity(solutionEntity)
                            .effectName(e.effectName())
                            .percent(e.percent())
                            .direction(DIRECTION.valueOf(e.direction()))
                            .build())
                    .collect(Collectors.toList());

            solutionEffectEntityRepository.saveAll(solutionEffectEntities);

            // 3. SolutionKeywordEntity 저장
            List<SolutionKeywordEntity> solutionKeywordEntities = request.keyword().stream()
                    .map(k -> SolutionKeywordEntity.builder()
                            .solutionEntity(solutionEntity)
                            .keyword(k)
                            .build())
                    .collect(Collectors.toList());

            solutionKeywordEntityRepository.saveAll(solutionKeywordEntities);

            return new SaveErpEntityResponse(solutionEntity.getSolutionSeq());

        } catch (DataIntegrityViolationException e) {
            log.error("Solution Service saveSolutionEntity Method DataIntegrityViolationException-> {}", e.getMessage());

            throw new ConflictException(ConflictErrorResult.IDEMPOTENT_REQUEST_CONFLICT_EXCEPTION);
        } catch (Exception e) {
            log.error("Solution Service saveSolutionEntity Method Exception -> {}", e.getMessage());

            throw new ServerException(ServerErrorResult.INTERNAL_SERVER_EXCEPTION);
        }
    }

    @Transactional
    public void modifyErpEntity(SaveErpEntityRequest request, MultipartFile representImageUrl, MultipartFile descriptionPdfUrl) {
        /*
         * [예외 처리]
         * 1. vendor 유효성 검사
         * 2. solution 유효성 검사
         * 3. 멱등성 오류
         * 4. 서버 오류
         * */
        vendorEntityRepository.findByVendorSeq(request.vendorSeq())
                .orElseThrow(() -> new NotFoundException(NotFoundErrorResult.VENDOR_NOT_FOUND_EXCEPTION));
        ErpEntity erpEntity = erpEntityRepository.findByVendorSeqAndCategory(request.vendorSeq(), CATEGORY.valueOf(request.category()))
                .orElseThrow(() -> new NotFoundException(NotFoundErrorResult.SOLUTION_NOT_FOUND_EXCEPTION));

        try {
            // 1. ErpEntity -> SolutionEntity 업데이트
            String S3RepresentImageUrl = commonService.uploadJPGFile(representImageUrl);
            String S3DescriptionPdfUrl = commonService.uploadPDFFile(descriptionPdfUrl);

            ErpEntity updatedErpEntity = erpEntity.updateErpEntity(
                    request.solutionName(),
                    request.solutionDetail(),
                    request.industry(),
                    request.recommendedCompanySize(),
                    request.solutionImplementationType(),
                    request.amount(),
                    SELL_TYPE.valueOf(request.sellType()),
                    request.duration(),
                    request.specialist(),
                    S3RepresentImageUrl,
                    S3DescriptionPdfUrl
            );

            SolutionEntity solutionEntity = solutionEntityRepository.saveSolutionEntity(updatedErpEntity);

            // 2. SolutionEffectEntity 삭제 후 저장
            solutionEffectEntityRepository.deleteAllBySolutionSeq(solutionEntity.getSolutionSeq());
            List<SolutionEffectEntity> solutionEffectEntities = Optional.ofNullable(request.solutionEffect())
                    .orElse(List.of())
                    .stream()
                    .map(e -> SolutionEffectEntity.builder()
                            .solutionEntity(solutionEntity)
                            .effectName(e.effectName())
                            .percent(e.percent())
                            .direction(DIRECTION.valueOf(e.direction()))
                            .build())
                    .collect(Collectors.toList());

            solutionEffectEntityRepository.saveAll(solutionEffectEntities);

            // 3. keywordEntity 삭제 후 저장
            solutionKeywordEntityRepository.deleteAllBySolutionSeq(solutionEntity.getSolutionSeq());
            List<SolutionKeywordEntity> solutionKeywordEntities = request.keyword().stream()
                    .map(k -> SolutionKeywordEntity.builder()
                            .solutionEntity(solutionEntity)
                            .keyword(k)
                            .build())
                    .collect(Collectors.toList());

            solutionKeywordEntityRepository.saveAll(solutionKeywordEntities);
        } catch (DataIntegrityViolationException e) {
            log.error("Solution Service modifySolutionEntity Method DataIntegrityViolationException-> {}", e.getMessage());

            throw new ConflictException(ConflictErrorResult.IDEMPOTENT_REQUEST_CONFLICT_EXCEPTION);
        } catch (Exception e) {
            log.error("Solution Service modifySolutionEntity Method Exception -> {}", e.getMessage());

            throw new ServerException(ServerErrorResult.INTERNAL_SERVER_EXCEPTION);
        }
    }
}
