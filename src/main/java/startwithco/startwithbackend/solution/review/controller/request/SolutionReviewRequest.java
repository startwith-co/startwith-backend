package startwithco.startwithbackend.solution.review.controller.request;

import org.springframework.http.HttpStatus;
import startwithco.startwithbackend.exception.BadRequestException;

import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.*;
import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.getCode;

public class SolutionReviewRequest {
    public record SaveSolutionReviewRequest(
            Long solutionSeq,
            Long consumerSeq,
            String comment,
            Double star
    ) {
        public void validate() {
            if (solutionSeq == null || consumerSeq == null || comment == null || star == null) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
                );
            }
        }
    }

    public record ModifySolutionReviewRequest(
            Long solutionReviewSeq,
            Long solutionSeq,
            Long consumerSeq,
            String comment,
            Double star
    ) {
        public void validate() {
            if (solutionReviewSeq == null || solutionSeq == null || consumerSeq == null || comment == null || star == null) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
                );
            }
        }
    }

    public record DeleteSolutionReviewRequest(
            Long solutionReviewSeq,
            Long solutionSeq,
            Long consumerSeq
    ) {
        public void validate() {
            if (solutionReviewSeq == null || solutionSeq == null || consumerSeq == null) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
                );
            }
        }
    }
}
