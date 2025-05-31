package startwithco.startwithbackend.solution.solution.controller.response;

import startwithco.startwithbackend.solution.effect.util.DIRECTION;

import java.util.List;

public class SolutionResponse {
    public record SaveSolutionEntityResponse(
            Long solutionSeq
    ) {

    }

    public record GetSolutionEntityResponse(
            Long solutionSeq,
            String representImageUrl,
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
}
