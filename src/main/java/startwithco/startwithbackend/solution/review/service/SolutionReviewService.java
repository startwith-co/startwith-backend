package startwithco.startwithbackend.solution.review.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import startwithco.startwithbackend.b2b.consumer.domain.ConsumerEntity;
import startwithco.startwithbackend.b2b.consumer.repository.ConsumerRepository;
import startwithco.startwithbackend.exception.ConflictException;
import startwithco.startwithbackend.exception.NotFoundException;
import startwithco.startwithbackend.solution.review.domain.SolutionReviewEntity;
import startwithco.startwithbackend.solution.review.repository.SolutionReviewEntityRepository;
import startwithco.startwithbackend.solution.solution.domain.SolutionEntity;
import startwithco.startwithbackend.solution.solution.repository.SolutionEntityRepository;

import java.util.List;

import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.*;
import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.getCode;
import static startwithco.startwithbackend.solution.review.controller.request.SolutionReviewRequest.*;
import static startwithco.startwithbackend.solution.review.controller.response.SolutionReviewResponse.*;

@Service
@RequiredArgsConstructor
public class SolutionReviewService {
    private final SolutionReviewEntityRepository solutionReviewEntityRepository;
    private final ConsumerRepository consumerRepository;
    private final SolutionEntityRepository solutionEntityRepository;

    @Transactional
    public SaveSolutionReviewResponse saveSolutionReviewEntity(SaveSolutionReviewRequest request) {
        ConsumerEntity consumerEntity = consumerRepository.findByConsumerSeq(request.consumerSeq())
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 수요 기업입니다.",
                        getCode("존재하지 않는 수요 기업입니다.", ExceptionType.BAD_REQUEST)
                ));
        SolutionEntity solutionEntity = solutionEntityRepository.findBySolutionSeq(request.solutionSeq())
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 솔루션입니다.",
                        getCode("존재하지 않는 솔루션입니다.", ExceptionType.BAD_REQUEST)
                ));

        try {
            SolutionReviewEntity solutionReviewEntity = SolutionReviewEntity.builder()
                    .consumerEntity(consumerEntity)
                    .solutionEntity(solutionEntity)
                    .comment(request.comment())
                    .star(request.star())
                    .build();

            SolutionReviewEntity savedSolutionReviewEntity = solutionReviewEntityRepository.saveSolutionReviewEntity(solutionReviewEntity);

            return new SaveSolutionReviewResponse(savedSolutionReviewEntity.getSolutionReviewSeq());
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(
                    HttpStatus.CONFLICT.value(),
                    "동시성 저장은 불가능합니다.",
                    getCode("동시성 저장은 불가능합니다.", ExceptionType.CONFLICT)
            );
        }
    }

    public void modifySolutionReviewEntity(ModifySolutionReviewRequest request) {
        consumerRepository.findByConsumerSeq(request.consumerSeq())
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 수요 기업입니다.",
                        getCode("존재하지 않는 수요 기업입니다.", ExceptionType.BAD_REQUEST)
                ));
        solutionEntityRepository.findBySolutionSeq(request.solutionSeq())
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 솔루션입니다.",
                        getCode("존재하지 않는 솔루션입니다.", ExceptionType.BAD_REQUEST)
                ));
        SolutionReviewEntity solutionReviewEntity
                = solutionReviewEntityRepository.findBySolutionSeqAndConsumerSeqAndSolutionReviewSeq(request.solutionSeq(), request.consumerSeq(), request.solutionReviewSeq())
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "수요 기업이 해당 솔루션에 작성한 리뷰가 없습니다.",
                        getCode("수요 기업이 해당 솔루션에 작성한 리뷰가 없습니다.", ExceptionType.BAD_REQUEST)
                ));

        solutionReviewEntity.updateSolutionReviewEntity(request.star(), request.comment());
        solutionReviewEntityRepository.saveSolutionReviewEntity(solutionReviewEntity);
    }

    public void deleteSolutionReviewEntity(DeleteSolutionReviewRequest request) {
        consumerRepository.findByConsumerSeq(request.consumerSeq())
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 수요 기업입니다.",
                        getCode("존재하지 않는 수요 기업입니다.", ExceptionType.BAD_REQUEST)
                ));
        solutionEntityRepository.findBySolutionSeq(request.solutionSeq())
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 솔루션입니다.",
                        getCode("존재하지 않는 솔루션입니다.", ExceptionType.BAD_REQUEST)
                ));
        SolutionReviewEntity solutionReviewEntity
                = solutionReviewEntityRepository.findBySolutionSeqAndConsumerSeqAndSolutionReviewSeq(request.solutionSeq(), request.consumerSeq(), request.solutionReviewSeq())
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "수요 기업이 해당 솔루션에 작성한 리뷰가 없습니다.",
                        getCode("수요 기업이 해당 솔루션에 작성한 리뷰가 없습니다.", ExceptionType.BAD_REQUEST)
                ));

        solutionReviewEntityRepository.deleteSolutionReviewEntity(solutionReviewEntity);
    }

    public List<GetAllSolutionReviewResponse> getAllSolutionReviewEntity(Long solutionSeq) {
        solutionEntityRepository.findBySolutionSeq(solutionSeq)
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 솔루션입니다.",
                        getCode("존재하지 않는 솔루션입니다.", ExceptionType.BAD_REQUEST)
                ));

        return solutionReviewEntityRepository.findAllBySolutionSeq(solutionSeq);
    }
}
