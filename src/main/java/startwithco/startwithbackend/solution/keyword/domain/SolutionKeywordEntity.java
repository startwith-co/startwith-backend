package startwithco.startwithbackend.solution.keyword.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import startwithco.startwithbackend.base.BaseTimeEntity;
import startwithco.startwithbackend.solution.solution.domain.SolutionEntity;

@Entity
@Table(name = "SOLUTION_KEYWORD_ENTITY")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@SuperBuilder
public class SolutionKeywordEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "solution_keyword_seq")
    private Long solutionKeywordSeq;

    @Column(name = "keyword", nullable = false)
    private String keyword;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solution_seq")
    private SolutionEntity solutionEntity;
}
