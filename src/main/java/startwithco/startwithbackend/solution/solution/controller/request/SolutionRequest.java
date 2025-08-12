package startwithco.startwithbackend.solution.solution.controller.request;

import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import startwithco.startwithbackend.exception.BadRequestException;
import startwithco.startwithbackend.exception.code.ExceptionCodeMapper;
import startwithco.startwithbackend.solution.solution.util.CATEGORY;
import startwithco.startwithbackend.solution.effect.util.DIRECTION;

import java.util.List;

import static io.micrometer.common.util.StringUtils.isBlank;
import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.getCode;

public class SolutionRequest {
    public record SaveSolutionEntityRequest(
            // 솔루션 기본 정보 입력
            Long vendorSeq,
            String solutionName,
            String solutionDetail,
            String category,
            String industry,
            String recommendedCompanySize,
            String solutionImplementationType,

            // 판매 정보 입력
            Long amount,
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
                    isBlank(solutionName) ||
                    isBlank(solutionDetail) ||
                    category == null ||
                    isBlank(industry) ||
                    isBlank(recommendedCompanySize) ||
                    isBlank(solutionImplementationType) ||
                    amount == null ||
                    duration == null ||
                    CollectionUtils.isEmpty(keyword)) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
                );
            }

            try {
                CATEGORY.valueOf(category.toUpperCase());

                if (!CollectionUtils.isEmpty(solutionEffect)) {
                    for (SolutionEffectEntityRequest effect : solutionEffect) {
                        DIRECTION.valueOf(effect.direction().toUpperCase());
                    }
                }
            } catch (Exception e) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
                );
            }

            if (amount < 0) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
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
            String industry,
            String recommendedCompanySize,
            String solutionImplementationType,

            // 판매 정보 입력
            Long amount,
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
                    isBlank(solutionName) ||
                    isBlank(solutionDetail) ||
                    prevCategory == null ||
                    nextCategory == null ||
                    isBlank(industry) ||
                    isBlank(recommendedCompanySize) ||
                    isBlank(solutionImplementationType) ||
                    amount == null ||
                    duration == null ||
                    CollectionUtils.isEmpty(keyword)) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
                );
            }

            try {
                CATEGORY.valueOf(prevCategory.toUpperCase());
                CATEGORY.valueOf(nextCategory.toUpperCase());

                if (!CollectionUtils.isEmpty(solutionEffect)) {
                    for (SolutionEffectEntityRequest effect : solutionEffect) {
                        DIRECTION.valueOf(effect.direction().toUpperCase());
                    }
                }
            } catch (Exception e) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
                );
            }

            if (amount < 0) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
                );
            }
        }
    }
}
