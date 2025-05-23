package startwithco.startwithbackend.solution.erp.controller.request;

import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import startwithco.startwithbackend.exception.badRequest.BadRequestErrorResult;
import startwithco.startwithbackend.exception.badRequest.BadRequestException;
import startwithco.startwithbackend.solution.solution.util.CATEGORY;
import startwithco.startwithbackend.solution.effect.util.DIRECTION;
import startwithco.startwithbackend.solution.solution.util.SELL_TYPE;

import java.util.List;

import static io.micrometer.common.util.StringUtils.isBlank;

public class ErpRequest {
    public record SaveErpEntityRequest(
            Long vendorSeq,
            String solutionName,
            String solutionDetail,
            String category,
            String industry,
            String recommendedCompanySize,
            String solutionImplementationType,
            String specialist,
            Long amount,
            String sellType,
            Long duration,
            List<SolutionEffectEntityRequest> solutionEffect,
            List<String> keyword
    ) {
        public record SolutionEffectEntityRequest(
                String effectName,
                Long percent,
                String direction
        ) {
            public void getParsedDirection() {
                try {
                    DIRECTION.valueOf(direction.toUpperCase());
                } catch (Exception e) {
                    throw new BadRequestException(BadRequestErrorResult.BAD_REQUEST_EXCEPTION);
                }
            }
        }

        public void validate(MultipartFile representImageUrl, MultipartFile descriptionPdfUrl) {
            if (vendorSeq == null ||
                    isBlank(solutionName) ||
                    isBlank(solutionDetail) ||
                    category == null ||
                    isBlank(industry) ||
                    isBlank(recommendedCompanySize) ||
                    isBlank(solutionImplementationType) ||
                    isBlank(specialist) ||
                    amount == null ||
                    sellType == null ||
                    duration == null ||
                    representImageUrl == null ||
                    descriptionPdfUrl == null ||
                    CollectionUtils.isEmpty(keyword)) {
                throw new BadRequestException(BadRequestErrorResult.BAD_REQUEST_EXCEPTION);
            }
        }

        public void getParsedCategory() {
            try {
                CATEGORY.valueOf(category.toUpperCase());
            } catch (Exception e) {
                throw new BadRequestException(BadRequestErrorResult.BAD_REQUEST_EXCEPTION);
            }
        }

        public void getParsedSellType() {
            try {
                SELL_TYPE.valueOf(sellType.toUpperCase());
            } catch (Exception e) {
                throw new BadRequestException(BadRequestErrorResult.BAD_REQUEST_EXCEPTION);
            }
        }
    }
}
