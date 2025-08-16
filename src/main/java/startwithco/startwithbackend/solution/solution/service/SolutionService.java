package startwithco.startwithbackend.solution.solution.service;

import jakarta.persistence.LockTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;
import startwithco.startwithbackend.b2b.vendor.repository.VendorEntityRepository;
import startwithco.startwithbackend.solution.review.repository.SolutionReviewEntityRepository;
import startwithco.startwithbackend.solution.solution.util.CATEGORY;
import startwithco.startwithbackend.solution.effect.util.DIRECTION;
import startwithco.startwithbackend.exception.ConflictException;
import startwithco.startwithbackend.exception.NotFoundException;
import startwithco.startwithbackend.solution.effect.domain.SolutionEffectEntity;
import startwithco.startwithbackend.solution.effect.repository.SolutionEffectEntityRepository;
import startwithco.startwithbackend.solution.keyword.domain.SolutionKeywordEntity;
import startwithco.startwithbackend.solution.keyword.repository.SolutionKeywordEntityRepository;
import startwithco.startwithbackend.solution.solution.domain.SolutionEntity;
import startwithco.startwithbackend.solution.solution.repository.SolutionEntityRepository;
import startwithco.startwithbackend.common.service.CommonService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.*;
import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.getCode;
import static startwithco.startwithbackend.solution.solution.controller.request.SolutionRequest.*;
import static startwithco.startwithbackend.solution.solution.controller.response.SolutionResponse.*;
import static startwithco.startwithbackend.solution.solution.controller.response.SolutionResponse.GetSolutionEntityResponse.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SolutionService {
    private final VendorEntityRepository vendorEntityRepository;
    private final SolutionEntityRepository solutionEntityRepository;
    private final SolutionEffectEntityRepository solutionEffectEntityRepository;
    private final SolutionKeywordEntityRepository solutionKeywordEntityRepository;
    private final SolutionReviewEntityRepository solutionReviewEntityRepository;

    private final CommonService commonService;

    @Transactional
    public SaveSolutionEntityResponse saveSolutionEntity(SaveSolutionEntityRequest request, MultipartFile representImageUrl, MultipartFile descriptionPdfUrl) {
        VendorEntity vendorEntity = vendorEntityRepository.findByVendorSeqForUpdate(request.vendorSeq())
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 벤더 기업입니다.",
                        getCode("존재하지 않는 벤더 기업입니다.", ExceptionType.NOT_FOUND)
                ));

        solutionEntityRepository.findByVendorSeqAndCategory(request.vendorSeq(), CATEGORY.valueOf(request.category()))
                .ifPresent(solutionEntity -> {
                    throw new ConflictException(
                            HttpStatus.CONFLICT.value(),
                            "해당 벤더의 해당 카테고리 솔루션이 이미 존재합니다.",
                            getCode("해당 벤더의 해당 카테고리 솔루션이 이미 존재합니다.", ExceptionType.CONFLICT)
                    );
                });

        try {
            String s3RepresentImageUrl = commonService.uploadJPGFile(representImageUrl);
            String s3DescriptionPdfUrl = commonService.uploadPDFFile(descriptionPdfUrl);

            SolutionEntity solutionEntity = SolutionEntity.builder()
                    .vendorEntity(vendorEntity)
                    .solutionName(request.solutionName())
                    .solutionDetail(request.solutionDetail())
                    .category(CATEGORY.valueOf(request.category()))
                    .industry(request.industry())
                    .recommendedCompanySize(request.recommendedCompanySize())
                    .solutionImplementationType(request.solutionImplementationType())
                    .amount(request.amount())
                    .duration(request.duration())
                    .representImageUrl(s3RepresentImageUrl)
                    .descriptionPdfUrl(s3DescriptionPdfUrl)
                    .deleted(false)
                    .build();

            solutionEntityRepository.saveSolutionEntity(solutionEntity);

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

            List<SolutionKeywordEntity> solutionKeywordEntities = request.keyword().stream()
                    .map(k -> SolutionKeywordEntity.builder()
                            .solutionEntity(solutionEntity)
                            .keyword(k)
                            .build())
                    .collect(Collectors.toList());

            solutionKeywordEntityRepository.saveAll(solutionKeywordEntities);

            return new SaveSolutionEntityResponse(solutionEntity.getSolutionSeq());
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(
                    HttpStatus.CONFLICT.value(),
                    "동시성 저장은 불가능합니다.",
                    getCode("동시성 저장은 불가능합니다.", ExceptionType.CONFLICT)
            );
        }
    }

    @Transactional
    public void modifySolutionEntity(ModifySolutionEntityRequest request, MultipartFile representImageUrl, MultipartFile descriptionPdfUrl) {
        vendorEntityRepository.findByVendorSeqForUpdate(request.vendorSeq())
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 벤더 기업입니다.",
                        getCode("존재하지 않는 벤더 기업입니다.", ExceptionType.NOT_FOUND)
                ));

        CATEGORY prevCat = CATEGORY.valueOf(request.prevCategory());
        CATEGORY nextCat = CATEGORY.valueOf(request.nextCategory());

        SolutionEntity solutionEntity = solutionEntityRepository.findByVendorSeqAndCategory(request.vendorSeq(), prevCat)
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 솔루션입니다.",
                        getCode("존재하지 않는 솔루션입니다.", ExceptionType.NOT_FOUND)
                ));

        if (!prevCat.equals(nextCat)) {
            solutionEntityRepository.findByVendorSeqAndCategory(request.vendorSeq(), nextCat)
                    .ifPresent(x -> { throw new ConflictException(
                            HttpStatus.CONFLICT.value(),
                            "이미 존재하는 카테고리입니다.",
                            getCode("이미 존재하는 카테고리입니다.", ExceptionType.CONFLICT)
                    );});
        }

        try {
            String s3RepresentImageUrl = commonService.uploadJPGFile(representImageUrl);
            String s3DescriptionPdfUrl = commonService.uploadPDFFile(descriptionPdfUrl);

            SolutionEntity updatedSolutionEntity = solutionEntity.updateSolutionEntity(
                    request.solutionName(),
                    request.solutionDetail(),
                    CATEGORY.valueOf(request.nextCategory()),
                    request.industry(),
                    request.recommendedCompanySize(),
                    request.solutionImplementationType(),
                    request.amount(),
                    request.duration(),
                    s3RepresentImageUrl,
                    s3DescriptionPdfUrl
            );

            solutionEntityRepository.saveSolutionEntity(updatedSolutionEntity);

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

            solutionKeywordEntityRepository.deleteAllBySolutionSeq(solutionEntity.getSolutionSeq());
            List<SolutionKeywordEntity> solutionKeywordEntities = request.keyword().stream()
                    .map(k -> SolutionKeywordEntity.builder()
                            .solutionEntity(solutionEntity)
                            .keyword(k)
                            .build())
                    .collect(Collectors.toList());

            solutionKeywordEntityRepository.saveAll(solutionKeywordEntities);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(
                    HttpStatus.CONFLICT.value(),
                    "동시성 저장은 불가능합니다.",
                    getCode("동시성 저장은 불가능합니다.", ExceptionType.CONFLICT)
            );
        }
    }

    @Transactional(readOnly = true)
    public GetSolutionEntityResponse getSolutionEntityByCategory(Long vendorSeq, CATEGORY category) {
        vendorEntityRepository.findByVendorSeq(vendorSeq)
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 벤더 기업입니다.",
                        getCode("존재하지 않는 벤더 기업입니다.", ExceptionType.NOT_FOUND)
                ));

        SolutionEntity solutionEntity = solutionEntityRepository.findByVendorSeqAndCategory(vendorSeq, category)
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "해당 기업이 작성한 카테고리 솔루션이 존재하지 않습니다.",
                        getCode("해당 기업이 작성한 카테고리 솔루션이 존재하지 않습니다.", ExceptionType.NOT_FOUND)
                ));

        List<String> solutionImplementationType = List.of(solutionEntity.getSolutionImplementationType().split(","));
        List<String> industry = List.of(solutionEntity.getIndustry().split(","));
        List<String> recommendedCompanySize = List.of(solutionEntity.getRecommendedCompanySize().split(","));
        List<SolutionEffectResponse> solutionEffectResponse
                = solutionEffectEntityRepository.findAllBySolutionSeqCustom(solutionEntity.getSolutionSeq());
        List<String> keywords = solutionKeywordEntityRepository.findAllKeywordsBySolutionSeq(solutionEntity.getSolutionSeq());

        return new GetSolutionEntityResponse(
                solutionEntity.getSolutionSeq(),
                solutionEntity.getRepresentImageUrl(),
                solutionEntity.getDescriptionPdfUrl(),
                solutionEntity.getSolutionName(),
                solutionEntity.getSolutionDetail(),
                solutionEntity.getAmount(),
                solutionImplementationType,
                solutionEntity.getDuration(),
                industry,
                recommendedCompanySize,
                solutionEffectResponse,
                keywords
        );
    }

    @Transactional(readOnly = true)
    public List<GetAllSolutionEntityResponse> getAllSolutionEntity(CATEGORY category, String industry, String budget, String keyword, int start, int end) {
        List<SolutionEntity> solutionEntities
                = solutionEntityRepository.findByCategoryAndIndustryAndBudgetAndKeyword(category, industry, budget, keyword, start, end);

        List<GetAllSolutionEntityResponse> response = new ArrayList<>();
        for (SolutionEntity solutionEntity : solutionEntities) {
            VendorEntity vendorEntity = solutionEntity.getVendorEntity();
            Long countSolutionReview = solutionReviewEntityRepository.countBySolutionSeq(solutionEntity.getSolutionSeq());
            Double averageStar = Optional.ofNullable(
                    solutionReviewEntityRepository.averageBySolutionSeq(solutionEntity.getSolutionSeq())
            ).orElse(0.0);

            response.add(new GetAllSolutionEntityResponse(
                    solutionEntity.getSolutionSeq(),
                    solutionEntity.getSolutionName(),
                    solutionEntity.getAmount(),
                    solutionEntity.getRepresentImageUrl(),
                    solutionEntity.getCategory(),
                    vendorEntity.getVendorSeq(),
                    vendorEntity.getVendorName(),
                    averageStar,
                    countSolutionReview
            ));
        }

        return response;
    }

    @Transactional(readOnly = true)
    public GetSolutionEntityResponse getSolutionEntity(Long solutionSeq) {
        SolutionEntity solutionEntity = solutionEntityRepository.findBySolutionSeq(solutionSeq)
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "해당 기업이 작성한 카테고리 솔루션이 존재하지 않습니다.",
                        getCode("해당 기업이 작성한 카테고리 솔루션이 존재하지 않습니다.", ExceptionType.NOT_FOUND)
                ));

        List<String> solutionImplementationType = List.of(solutionEntity.getSolutionImplementationType().split(","));
        List<String> industry = List.of(solutionEntity.getIndustry().split(","));
        List<String> recommendedCompanySize = List.of(solutionEntity.getRecommendedCompanySize().split(","));
        List<SolutionEffectResponse> solutionEffectResponse
                = solutionEffectEntityRepository.findAllBySolutionSeqCustom(solutionEntity.getSolutionSeq());
        List<String> keywords = solutionKeywordEntityRepository.findAllKeywordsBySolutionSeq(solutionEntity.getSolutionSeq());

        return new GetSolutionEntityResponse(
                solutionEntity.getSolutionSeq(),
                solutionEntity.getRepresentImageUrl(),
                solutionEntity.getDescriptionPdfUrl(),
                solutionEntity.getSolutionName(),
                solutionEntity.getSolutionDetail(),
                solutionEntity.getAmount(),
                solutionImplementationType,
                solutionEntity.getDuration(),
                industry,
                recommendedCompanySize,
                solutionEffectResponse,
                keywords
        );
    }

    @Transactional
    public void deleteSolutionEntity(Long solutionSeq) {
        SolutionEntity solutionEntity = solutionEntityRepository.findBySolutionSeq(solutionSeq)
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "해당 기업이 작성한 카테고리 솔루션이 존재하지 않습니다.",
                        getCode("해당 기업이 작성한 카테고리 솔루션이 존재하지 않습니다.", ExceptionType.NOT_FOUND)
                ));

        solutionEntity.deleteSolutionEntity();
        solutionEntityRepository.saveSolutionEntity(solutionEntity);
    }
}
