package startwithco.startwithbackend.solution.review.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import startwithco.startwithbackend.b2b.consumer.domain.ConsumerEntity;
import startwithco.startwithbackend.base.BaseTimeEntity;
import startwithco.startwithbackend.solution.solution.domain.SolutionEntity;

@Entity
@Table(name = "SOLUTION_REVIEW_ENTITY")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@SuperBuilder
public class SolutionReviewEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "solution_review_seq")
    private Long solutionReviewSeq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solution_seq")
    private SolutionEntity solutionEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer_seq", nullable = false)
    private ConsumerEntity consumerEntity;

    @Column(name = "star", nullable = false)
    private Double star;

    @Lob
    @Column(name = "comment", nullable = false)
    private String comment;
}
