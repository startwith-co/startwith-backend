package startwithco.startwithbackend.b2b.vendor.controller.response;

import lombok.Builder;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;
import startwithco.startwithbackend.payment.paymentEvent.util.PAYMENT_EVENT_STATUS;
import startwithco.startwithbackend.solution.solution.util.CATEGORY;

import java.time.LocalDateTime;
import java.time.LocalTime;

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

    public record LoginVendorResponse(
            String accessToken,
            String refreshToken,
            Long vendorSeq
    ) {}

    @Builder
    public record GetVendorInfo(
            Long vendorSeq,
            String vendorName,
            String managerName,
            String phoneNumber,
            String email,
            boolean audit,
            String accountNumber,
            String bank,
            String vendorExplanation,
            String vendorBannerImageUrl,
            boolean weekdayAvailable,
            LocalTime weekdayStartTime,
            LocalTime weekdayEndTime,
            boolean weekendAvailable,
            LocalTime weekendStartTime,
            LocalTime weekendEndTime,
            boolean holidayAvailable,
            LocalTime holidayStartTime,
            LocalTime holidayEndTime,
            Long orderCount,
            Long clientCount
    ) {
        public static GetVendorInfo fromEntity(VendorEntity vendorEntity) {
            return GetVendorInfo.builder()
                    .vendorSeq(vendorEntity.getVendorSeq())
                    .vendorName(vendorEntity.getVendorName())
                    .managerName(vendorEntity.getManagerName())
                    .phoneNumber(vendorEntity.getPhoneNumber())
                    .email(vendorEntity.getEmail())
                    .audit(vendorEntity.isAudit())
                    .accountNumber(vendorEntity.getAccountNumber())
                    .bank(vendorEntity.getBank())
                    .vendorExplanation(vendorEntity.getVendorExplanation())
                    .vendorBannerImageUrl(vendorEntity.getVendorBannerImageUrl())
                    .weekdayAvailable(vendorEntity.isWeekdayAvailable())
                    .weekdayStartTime(vendorEntity.getWeekdayStartTime())
                    .weekdayEndTime(vendorEntity.getWeekdayEndTime())
                    .weekendAvailable(vendorEntity.isWeekendAvailable())
                    .weekendStartTime(vendorEntity.getWeekendStartTime())
                    .weekendEndTime(vendorEntity.getWeekendEndTime())
                    .holidayAvailable(vendorEntity.isHolidayAvailable())
                    .holidayStartTime(vendorEntity.getHolidayStartTime())
                    .holidayEndTime(vendorEntity.getHolidayEndTime())
                    .orderCount(vendorEntity.getOrderCount())
                    .clientCount(vendorEntity.getClientCount())
                    .build();
        }
    }
}
