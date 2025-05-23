package startwithco.startwithbackend.payment.status.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import startwithco.startwithbackend.base.BaseTimeEntity;
import startwithco.startwithbackend.payment.payment.domain.PaymentEntity;
import startwithco.startwithbackend.util.CONSUMER_STATUS;
import startwithco.startwithbackend.util.VENDOR_STATUS;

import java.time.LocalDateTime;

@Entity
@Table(name = "STATUS_ENTITY")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@SuperBuilder
public class StatusEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_seq")
    private Long statusSeq;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_seq", nullable = false)
    private PaymentEntity paymentEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "consumer_status", nullable = false)
    private CONSUMER_STATUS consumerStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "vendor_status", nullable = false)
    private VENDOR_STATUS vendorStatus;

    @Column(name = "payment_completed_at")
    private LocalDateTime paymentCompletedAt;

    @Column(name = "development_completed_at")
    private LocalDateTime developmentCompletedAt;

    @Column(name = "actual_duration")
    private Long actualDuration;
}
