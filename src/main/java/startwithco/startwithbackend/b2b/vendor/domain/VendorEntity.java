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

    public void update(String vendorName, String managerName, String phoneNumber, String email, boolean audit,
                       String accountNumber, String bank, String vendorExplanation, String vendorBannerImageUrl,
                       boolean weekdayAvailable, LocalTime weekdayStartTime, LocalTime weekdayEndTime,
                       boolean weekendAvailable, LocalTime weekendStartTime, LocalTime weekendEndTime,
                       boolean holidayAvailable, LocalTime holidayStartTime, LocalTime holidayEndTime,
                       Long orderCount, Long clientCount) {
        this.vendorName = vendorName;
        this.managerName = managerName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.audit = audit;
        this.accountNumber = accountNumber;
        this.bank = bank;
        this.vendorExplanation = vendorExplanation;
        this.vendorBannerImageUrl = vendorBannerImageUrl;
        this.weekdayAvailable = weekdayAvailable;
        this.weekdayStartTime = weekdayStartTime;
        this.weekdayEndTime = weekdayEndTime;
        this.weekendAvailable = weekendAvailable;
        this.weekendStartTime = weekendStartTime;
        this.weekendEndTime = weekendEndTime;
        this.holidayAvailable = holidayAvailable;
        this.holidayStartTime = holidayStartTime;
        this.holidayEndTime = holidayEndTime;
        this.orderCount = orderCount;
        this.clientCount = clientCount;
    }

    public void updatePassword(String encodedPassword) {
        this.encodedPassword = encodedPassword;
    }
}
