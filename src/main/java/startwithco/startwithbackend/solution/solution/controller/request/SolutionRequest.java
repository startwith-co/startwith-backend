package startwithco.startwithbackend.solution.solution.controller.request;

import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import startwithco.startwithbackend.exception.BadRequestException;
import startwithco.startwithbackend.solution.solution.util.CATEGORY;
import startwithco.startwithbackend.solution.effect.util.DIRECTION;

import java.util.List;

import static io.micrometer.common.util.StringUtils.isBlank;
import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.*;
import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.getCode;

public class SolutionRequest {
    public record SaveSolutionEntityRequest(
            // 솔루션 기본 정보 입력
            Long vendorSeq,
            String solutionName,
            String solutionDetail,
            String category,
            String recommendedCompanySize,
            String solutionImplementationType,

            // 판매 정보 입력
            Double amount,
            Long duration,

            // 솔루션 상세 정보 입력
            List<SolutionEffectEntityRequest> solutionEffect,

            // 키워드 검색 태그
            List<String> keyword
    ) {
        public record SolutionEffectEntityRequest(
                String effectName,
                Long percent,
                String direction
        ) {

        }

        public void validate() {
            if (vendorSeq == null ||
                    isBlank(solutionName) || solutionName.length() > 30 ||
                    isBlank(solutionDetail) ||
                    category == null ||
                    isBlank(recommendedCompanySize) ||
                    isBlank(solutionImplementationType) ||
                    duration == null ||
                    CollectionUtils.isEmpty(keyword) ||
                    amount == null || amount < 0 || amount > 10_000_000 ||
                    keyword.size() > 10) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
                );
            }

            if (amount % 1 != 0) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "금액은 정수만 입력 가능합니다.",
                        getCode("금액은 정수만 입력 가능합니다.", ExceptionType.BAD_REQUEST)
                );
            }

            try {
                CATEGORY.valueOf(category.toUpperCase());

                if (!CollectionUtils.isEmpty(solutionEffect)) {
                    boolean hasInvalidDirection = solutionEffect.stream()
                            .anyMatch(effect -> {
                                try {
                                    DIRECTION.valueOf(effect.direction().toUpperCase());
                                    return false;
                                } catch (Exception e) {
                                    return true;
                                }
                            });
                    if (hasInvalidDirection) {
                        throw new BadRequestException(
                                HttpStatus.BAD_REQUEST.value(),
                                "요청 데이터 오류입니다.",
                                getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
                        );
                    }
                }
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        e.getMessage(),
                        getCode(e.getMessage(), ExceptionType.BAD_REQUEST)
                );
            }
        }
    }

    public record ModifySolutionEntityRequest(
            // 솔루션 기본 정보 입력
            Long vendorSeq,
            String solutionName,
            String solutionDetail,
            String prevCategory,
            String nextCategory,
            String recommendedCompanySize,
            String solutionImplementationType,

            // 판매 정보 입력
            Double amount,
            Long duration,

            // 솔루션 상세 정보 입력
            List<SolutionEffectEntityRequest> solutionEffect,

            // 키워드 검색 태그
            List<String> keyword
    ) {
        public record SolutionEffectEntityRequest(
                String effectName,
                Long percent,
                String direction
        ) {

        }

        public void validate() {
            if (vendorSeq == null ||
                    isBlank(solutionName) || solutionName.length() > 30 ||
                    isBlank(solutionDetail) ||
                    prevCategory == null ||
                    nextCategory == null ||
                    isBlank(recommendedCompanySize) ||
                    isBlank(solutionImplementationType) ||
                    amount == null || amount < 0 || amount > 10_000_000 ||
                    duration == null ||
                    CollectionUtils.isEmpty(keyword) ||
                    keyword.size() > 10) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
                );
            }

            if (amount % 1 != 0) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "금액은 정수만 입력 가능합니다.",
                        getCode("금액은 정수만 입력 가능합니다.", ExceptionType.BAD_REQUEST)
                );
            }

            try {
                CATEGORY.valueOf(prevCategory.toUpperCase());
                CATEGORY.valueOf(nextCategory.toUpperCase());

                if (!CollectionUtils.isEmpty(solutionEffect)) {
                    boolean hasInvalidDirection = solutionEffect.stream()
                            .anyMatch(effect -> {
                                try {
                                    DIRECTION.valueOf(effect.direction().toUpperCase());
                                    return false;
                                } catch (Exception e) {
                                    return true;
                                }
                            });
                    if (hasInvalidDirection) {
                        throw new BadRequestException(
                                HttpStatus.BAD_REQUEST.value(),
                                "요청 데이터 오류입니다.",
                                getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
                        );
                    }
                }
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
                );
            }

            boolean hasInvalidKeyword = keyword.stream()
                    .anyMatch(k -> k.length() > 20);
            if (hasInvalidKeyword) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
                );
            }
        }
    }
}
