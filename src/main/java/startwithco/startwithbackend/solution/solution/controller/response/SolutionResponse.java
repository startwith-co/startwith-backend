package startwithco.startwithbackend.solution.solution.controller.response;

import startwithco.startwithbackend.solution.effect.util.DIRECTION;
import startwithco.startwithbackend.solution.solution.util.CATEGORY;

import java.util.List;

public class SolutionResponse {
    public record SaveSolutionEntityResponse(
            Long solutionSeq
    ) {

    }

    public record GetSolutionEntityResponse(
            Long solutionSeq,
            String representImageUrl,
            String descriptionPdfUrl,
            String solutionName,
            String solutionDetail,
            Long amount,
            List<String> solutionImplementationType,
            Long duration,
            List<String> industry,
            List<String> recommendedCompanySize,
            List<SolutionEffectResponse> solutionEffect
    ) {
        public record SolutionEffectResponse(
                String effectName,
                Long percent,
                DIRECTION direction
        ) {

        }
    }

    public record GetAllSolutionEntityResponse(
            Long solutionSeq,
            String solutionName,
            Long amount,
            String representImageUrl,
            CATEGORY category,

            Long vendorSeq,
            String vendorName,

            Double averageStar,
            Long countSolutionReview
    ) {
    }
}
