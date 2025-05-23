package startwithco.startwithbackend.solution.erp.service;

import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;
import startwithco.startwithbackend.b2b.vendor.repository.VendorEntityRepository;
import startwithco.startwithbackend.exception.conflict.ConflictErrorResult;
import startwithco.startwithbackend.exception.conflict.ConflictException;
import startwithco.startwithbackend.exception.server.ServerErrorResult;
import startwithco.startwithbackend.exception.server.ServerException;
import startwithco.startwithbackend.solution.effect.domain.SolutionEffectEntity;
import startwithco.startwithbackend.solution.effect.repository.SolutionEffectEntityRepository;
import startwithco.startwithbackend.solution.erp.controller.response.ErpResponse;
import startwithco.startwithbackend.solution.erp.domain.ErpEntity;
import startwithco.startwithbackend.solution.keyword.domain.SolutionKeywordEntity;
import startwithco.startwithbackend.solution.keyword.repository.SolutionKeywordEntityRepository;
import startwithco.startwithbackend.solution.solution.domain.SolutionEntity;
import startwithco.startwithbackend.solution.solution.repository.SolutionEntityRepository;
import startwithco.startwithbackend.util.S3Util;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static startwithco.startwithbackend.solution.erp.controller.request.ErpRequest.*;
import static startwithco.startwithbackend.solution.erp.controller.response.ErpResponse.*;

@lombok.extern.slf4j.Slf4j
@Service
@RequiredArgsConstructor
@Slf4j
public class ErpService {
    private final VendorEntityRepository vendorEntityRepository;
    private final SolutionEntityRepository solutionEntityRepository;
    private final SolutionEffectEntityRepository solutionEffectEntityRepository;
    private final SolutionKeywordEntityRepository solutionKeywordEntityRepository;

    private final S3Util s3Util;

    @Transactional
    public SaveErpEntityResponse saveErpEntity(SaveErpEntityRequest request, MultipartFile representImageUrl, MultipartFile descriptionPdfUrl) {
        /*
         * [예외 처리]
         * 1. vendor 유효성 검사
         * 2. 멱등성 오류
         * 3. 서버 오류
         * */
        VendorEntity vendorEntity = vendorEntityRepository.findByVendorSeq(request.vendorSeq());

        try {
            // 1. ErpEntity -> SolutionEntity 저장
            String S3RepresentImageUrl = s3Util.uploadJPGFile(representImageUrl);
            String S3DescriptionPdfUrl = s3Util.uploadPDFFile(descriptionPdfUrl);

            ErpEntity erpEntity = ErpEntity.builder()
                    .vendorEntity(vendorEntity)
                    .solutionName(request.solutionName())
                    .solutionDetail(request.solutionDetail())
                    .category(request.category())
                    .industry(request.industry())
                    .recommendedCompanySize(request.recommendedCompanySize())
                    .solutionImplementationType(request.solutionImplementationType())
                    .amount(request.amount())
                    .sellType(request.sellType())
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
                            .direction(e.direction())
                            .build())
                    .collect(Collectors.toList());

            solutionEffectEntityRepository.saveAllSolutionEffectEntities(solutionEffectEntities);

            // 3. SolutionKeywordEntity 저장
            List<SolutionKeywordEntity> solutionKeywordEntities = request.keyword().stream()
                    .map(k -> SolutionKeywordEntity.builder()
                            .solutionEntity(solutionEntity)
                            .keyword(k)
                            .build())
                    .collect(Collectors.toList());

            solutionKeywordEntityRepository.saveAllSolutionKeywordEntities(solutionKeywordEntities);

            return new SaveErpEntityResponse(solutionEntity.getSolutionSeq());

        } catch (DataIntegrityViolationException e) {
            log.error("Solution Service saveSolutionEntity Method DataIntegrityViolationException");

            throw new ConflictException(ConflictErrorResult.IDEMPOTENT_REQUEST_CONFLICT_EXCEPTION);
        } catch (Exception e) {
            log.error("Solution Service saveSolutionEntity Method Exception");

            throw new ServerException(ServerErrorResult.INTERNAL_SERVER_EXCEPTION);
        }
    }
}
