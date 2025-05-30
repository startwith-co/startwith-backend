package startwithco.startwithbackend.b2b.vendor.controller.response;

import startwithco.startwithbackend.payment.paymentEvent.util.PAYMENT_EVENT_STATUS;
import startwithco.startwithbackend.solution.solution.util.CATEGORY;

import java.time.LocalDateTime;

public class VendorResponse {
    public record GetVendorSolutionCategoryResponse(
            CATEGORY category,
            Long solutionSeq
    ) {

    }

    public record GetVendorSettlementManagementStatusResponse(
            Long vendorSeq,
            Long requested,
            Long confirmed,
            Long settled
    ) {

    }

    public record GetVendorSettlementManagementProgressResponse(
            Long vendorSeq,
            PAYMENT_EVENT_STATUS paymentEventStatus,
            String solutionName,
            Long solutionAmount,
            LocalDateTime autoConfirmScheduledAt,
            Long settlementAmount,
            Long consumerSeq,
            String consumerName
    ) {

    }
}
