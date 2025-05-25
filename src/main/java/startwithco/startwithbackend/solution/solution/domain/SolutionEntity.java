package startwithco.startwithbackend.solution.solution.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;
import startwithco.startwithbackend.base.BaseTimeEntity;
import startwithco.startwithbackend.solution.solution.util.CATEGORY;
import startwithco.startwithbackend.solution.solution.util.SELL_TYPE;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(
        name = "SOLUTION_ENTITY",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"vendor_seq", "category"})
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@SuperBuilder
public class SolutionEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "solution_seq")
    private Long solutionSeq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_seq", nullable = false)
    private VendorEntity vendorEntity;

    /*
     * [솔루션 기본 정보 입력]
     * 1. 솔루션명
     * 2. 솔루션 카테고리
     * 3. 도입 가능 산업군
     * 4. 도입 추천 기업 규모
     * 5. 솔루션 구축 형태
     */
    @Column(name = "solution_name", nullable = false)
    private String solutionName;

    @Lob
    @Column(name = "solution_detail", nullable = false)
    private String solutionDetail;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private CATEGORY category;

    @Column(name = "industry", nullable = false)
    private String industry;

    @Column(name = "recommended_company_size", nullable = false)
    private String recommendedCompanySize;

    @Column(name = "solution_implementation_type", nullable = false)
    private String solutionImplementationType;

    /*
     * [판매 정보 입력]
     * 1. 판매가
     * 2. 판매 형태
     * 3. 솔루션 가격
     * 4. 개발 기간
     * */
    @Column(name = "amount", nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "sell_type", nullable = false)
    private SELL_TYPE sellType;

    @Column(name = "duration", nullable = false)
    private Long duration;

    /*
     * [솔루션 상세 정보 입력]
     * 1. 대표 이미지
     * 2. 솔루션 상세 설명 PDF
     * */
    @Column(name = "represent_image_url", nullable = false)
    private String representImageUrl;

    @Column(name = "description_pdf_url", nullable = false)
    private String descriptionPdfUrl;

    /*
     * [솔루션 기본 정보 입력]
     * 1. 기능 특화
     */
    @Column(name = "specialist", nullable = false)
    private String specialist;

    public SolutionEntity updateSolutionEntity(String solutionName,
                                    String solutionDetail,
                                    String industry,
                                    String recommendedCompanySize,
                                    String solutionImplementationType,
                                    Long amount,
                                    SELL_TYPE sellType,
                                    Long duration,
                                    String specialist,
                                    String representImageUrl,
                                    String descriptionPdfUrl) {
        this.solutionName = solutionName;
        this.solutionDetail = solutionDetail;
        this.industry = industry;
        this.recommendedCompanySize = recommendedCompanySize;
        this.solutionImplementationType = solutionImplementationType;
        this.amount = amount;
        this.sellType = sellType;
        this.duration = duration;
        this.specialist = specialist;
        this.representImageUrl = representImageUrl;
        this.descriptionPdfUrl = descriptionPdfUrl;

        return this;
    }
}
