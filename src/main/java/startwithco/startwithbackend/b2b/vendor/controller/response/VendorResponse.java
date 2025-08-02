package startwithco.startwithbackend.b2b.vendor.controller.response;

import lombok.Builder;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;
import startwithco.startwithbackend.payment.payment.util.PAYMENT_STATUS;
import startwithco.startwithbackend.solution.solution.util.CATEGORY;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class VendorResponse {
    public record GetVendorSolutionCategoryResponse(
            CATEGORY category,
            Long solutionSeq
    ) {

    }

    public record LoginVendorResponse(
            String accessToken,
            String refreshToken,
            Long vendorSeq,
            String vendorUniqueType,
            String vendorName
    ) {
    }

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
            Long clientCount,
            String vendorUniqueType
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
                    .vendorUniqueType(vendorEntity.getVendorUniqueType())
                    .build();
        }
    }

    public record GetVendorDashboardResponse(
            Long vendorSeq,
            Long CONFIRMED,
            Long DONE,
            Long SETTLED
    ) {

    }

    public record GetVendorDashboardDONEListResponse(
            Long vendorSeq,
            PAYMENT_STATUS paymentStatus,
            Long solutionSeq,
            String solutionName,
            Long solutionAmount,
            LocalDateTime settlementDueDate,
            LocalDateTime settlementAmount,
            Long consumerSeq,
            String consumerName
    ) {

    }

    public record GetVendorDashboardSETTELEDListResponse(
            Long vendorSeq,
            PAYMENT_STATUS paymentStatus,
            Long solutionSeq,
            String solutionName,
            Long solutionAmount,
            LocalDateTime settlementDueDate,
            Long settlementAmount,
            Long consumerSeq,
            String consumerName
    ) {

    }

    public record ResetLinkResponse(
            String token,
            String link,
            Long consumerSeq
    ) {

    }

    public record GetVendorSolutionEntitiesResponse(
            Long solutionSeq,
            String solutionName,
            CATEGORY category,
            Long amount
    ) {

    }
}
