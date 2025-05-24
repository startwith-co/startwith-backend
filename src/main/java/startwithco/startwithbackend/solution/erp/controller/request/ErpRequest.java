package startwithco.startwithbackend.solution.erp.controller.request;

import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import startwithco.startwithbackend.exception.badRequest.BadRequestErrorResult;
import startwithco.startwithbackend.exception.badRequest.BadRequestException;
import startwithco.startwithbackend.common.util.CATEGORY;
import startwithco.startwithbackend.common.util.DIRECTION;
import startwithco.startwithbackend.common.util.SELL_TYPE;

import java.util.List;

import static io.micrometer.common.util.StringUtils.isBlank;

public class ErpRequest {
    public record SaveErpEntityRequest(
            Long vendorSeq,
            String solutionName,
            String solutionDetail,
            CATEGORY category,
            String industry,
            String recommendedCompanySize,
            String solutionImplementationType,
            String specialist,
            Long amount,
            SELL_TYPE sellType,
            Long duration,
            List<SolutionEffectEntityRequest> solutionEffect,
            List<String> keyword
    ) {
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

        public record SolutionEffectEntityRequest(
                String effectName,
                Long percent,
                DIRECTION direction
        ) {

        }
    }
}
