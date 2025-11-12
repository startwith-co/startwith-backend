package startwithco.startwithbackend.b2b.vendor.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import startwithco.startwithbackend.base.BaseTimeEntity;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "VENDOR_ENTITY")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@SuperBuilder
public class VendorEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vendor_seq")
    private Long vendorSeq;

    /*
     * [벤더 기업 회원가입]
     * 1. 기업명(사업자명)
     * 2. 담당자 성함
     * 3. 담당자 연락처
     * 4. 이메일
     * 5. 비밀번호
     * 6. 사업자 등록증
     * */
    @Column(name = "vendor_name", nullable = false)
    private String vendorName;

    @Column(name = "manager_name", nullable = false)
    private String managerName;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String encodedPassword;

    @Column(name = "business_license_image", nullable = false)
    private String businessLicenseImage;

    /*
     * [대시보드]
     * 1. 승인 여부
     * */
    @Column(name = "audit", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean audit = false;

    /*
     * [회원 정보 수정]
     * 1. 사업자 계좌번호
     * 2. 은행명
     * 3. 기업 상세 소개
     * 4. 벤더 상세페이지 배너 이미지
     * */
    @Column(name = "account_number", nullable = true)
    private String accountNumber;

    @Column(name = "bank", nullable = true)
    private String bank;

    @Lob
    @Column(name = "vendor_explanation", nullable = true)
    private String vendorExplanation;

    @Column(name = "vendor_banner_image_url", nullable = true)
    private String vendorBannerImageUrl;

    /*
     * [상담 가능 요일 및 시간 설정]
     * 1. 평일
     * 2. 주말
     * 3. 공휴일
     * */
    @Column(name = "weekday_available", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean weekdayAvailable = false;

    @Column(name = "weekday_start_time")
    private LocalTime weekdayStartTime;

    @Column(name = "weekday_end_time")
    private LocalTime weekdayEndTime;

    @Column(name = "weekend_available", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean weekendAvailable = false;

    @Column(name = "weekend_start_time")
    private LocalTime weekendStartTime;

    @Column(name = "weekend_end_time")
    private LocalTime weekendEndTime;

    @Column(name = "holiday_available", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean holidayAvailable = false;

    @Column(name = "holiday_start_time")
    private LocalTime holidayStartTime;

    @Column(name = "holiday_end_time")
    private LocalTime holidayEndTime;

    /*
     * [총 거래 건수 및 총 기업 고객 수 설정]
     * 1. 총 거래 건수
     * 2. 총 기업 고객 수
     * */
    @Column(name = "order_count", nullable = true)
    private Long orderCount;

    @Column(name = "client_count", nullable = true)
    private Long clientCount;

    @Column(name = "vendor_unique_type", nullable = false)
    private String vendorUniqueType;

    @Column(name = "profile_image", nullable = true)
    private String profileImage;

    public void update(String vendorName, String managerName, String phoneNumber, String email, Boolean audit,
                       String accountNumber, String bank, String vendorExplanation, String vendorBannerImageUrl,
                       Boolean weekdayAvailable, LocalTime weekdayStartTime, LocalTime weekdayEndTime,
                       Boolean weekendAvailable, LocalTime weekendStartTime, LocalTime weekendEndTime,
                       Boolean holidayAvailable, LocalTime holidayStartTime, LocalTime holidayEndTime,
                       Long orderCount, Long clientCount, String profileImage) {

        if (vendorName != null) this.vendorName = vendorName;
        if (managerName != null) this.managerName = managerName;
        if (phoneNumber != null) this.phoneNumber = phoneNumber;
        if (email != null) this.email = email;
        if (audit != null) this.audit = audit;
        if (accountNumber != null) this.accountNumber = accountNumber;
        if (bank != null) this.bank = bank;
        if (vendorExplanation != null) this.vendorExplanation = vendorExplanation;
        if (vendorBannerImageUrl != null) this.vendorBannerImageUrl = vendorBannerImageUrl;

        if (weekdayAvailable != null) this.weekdayAvailable = weekdayAvailable;
        if (weekdayStartTime != null) this.weekdayStartTime = weekdayStartTime;
        if (weekdayEndTime != null) this.weekdayEndTime = weekdayEndTime;

        if (weekendAvailable != null) this.weekendAvailable = weekendAvailable;
        if (weekendStartTime != null) this.weekendStartTime = weekendStartTime;
        if (weekendEndTime != null) this.weekendEndTime = weekendEndTime;

        if (holidayAvailable != null) this.holidayAvailable = holidayAvailable;
        if (holidayStartTime != null) this.holidayStartTime = holidayStartTime;
        if (holidayEndTime != null) this.holidayEndTime = holidayEndTime;

        if (orderCount != null) this.orderCount = orderCount;
        if (clientCount != null) this.clientCount = clientCount;
        if (profileImage != null) this.profileImage = profileImage;
    }

    public void updatePassword(String encodedPassword) {
        this.encodedPassword = encodedPassword;
    }

    public void updateAudit(boolean audit) {
        this.audit = audit;
    }
}
