package startwithco.startwithbackend.b2b.vendor.controller.response;

import startwithco.startwithbackend.common.util.CATEGORY;

public class VendorResponse {
    public record GetVendorSolutionCategory(
            CATEGORY category,
            Long solutionSeq
    ) {

    }
}
