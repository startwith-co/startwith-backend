package startwithco.startwithbackend.solution.solution.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;
import startwithco.startwithbackend.base.BaseTimeEntity;
import startwithco.startwithbackend.solution.solution.util.CATEGORY;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@SQLDelete(sql = "UPDATE SOLUTION_ENTITY SET deleted = true WHERE solution_seq = ?")
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

    @Column(name = "solution_name", nullable = false)
    private String solutionName;

    @Lob
    @Column(name = "solution_detail", nullable = false, columnDefinition = "MEDIUMTEXT")
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

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "duration", nullable = false)
    private Long duration;

    @Column(name = "represent_image_url", nullable = false)
    private String representImageUrl;

    @Column(name = "description_pdf_url", nullable = false)
    private String descriptionPdfUrl;

    @Column(name = "deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean deleted = false;

    public SolutionEntity updateSolutionEntity(String solutionName,
                                               String solutionDetail,
                                               CATEGORY category,
                                               String industry,
                                               String recommendedCompanySize,
                                               String solutionImplementationType,
                                               Long amount,
                                               Long duration,
                                               String representImageUrl,
                                               String descriptionPdfUrl) {
        this.solutionName = solutionName;
        this.solutionDetail = solutionDetail;
        this.category = category;
        this.industry = industry;
        this.recommendedCompanySize = recommendedCompanySize;
        this.solutionImplementationType = solutionImplementationType;
        this.amount = amount;
        this.duration = duration;
        this.representImageUrl = representImageUrl;
        this.descriptionPdfUrl = descriptionPdfUrl;

        return this;
    }

    public void deleteSolutionEntity() {
        this.deleted = true;
    }
}
