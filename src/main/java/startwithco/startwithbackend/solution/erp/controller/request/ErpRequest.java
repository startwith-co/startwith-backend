package startwithco.startwithbackend.solution.erp.controller.request;

import startwithco.startwithbackend.util.CATEGORY;
import startwithco.startwithbackend.util.DIRECTION;
import startwithco.startwithbackend.util.SELL_TYPE;

import java.util.List;

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

    }

    public record SolutionEffectEntityRequest(
            String effectName,
            Long percent,
            DIRECTION direction
    ) {

    }
}
