package startwithco.startwithbackend.solution.erp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import startwithco.startwithbackend.solution.domain.SolutionEntity;

@Entity
@Table(name = "ERP_ENTITY")
@AllArgsConstructor
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
@Getter
@SuperBuilder
public class ErpEntity extends SolutionEntity {
    /*
     * [솔루션 기본 정보 입력]
     * 1. ERP 기능 특화
     */
    @Column(name = "specialist", nullable = false)
    private String specialist;

    /*
     * [솔루션 상세 정보 입력]
     * 1. 대표 이미지
     * 2. 솔루션 상세 설명 PDF
     * */
    @Column(name = "represent_image_url", nullable = false)
    private String representImageUrl;

    @Column(name = "description_pdf_url", nullable = false)
    private String descriptionPdfUrl;
}