package startwithco.startwithbackend.payment.paymentEvent.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import startwithco.startwithbackend.b2b.consumer.domain.ConsumerEntity;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;
import startwithco.startwithbackend.base.BaseTimeEntity;
import startwithco.startwithbackend.payment.paymentEvent.util.PAYMENT_EVENT_STATUS;
import startwithco.startwithbackend.solution.solution.util.SELL_TYPE;
import startwithco.startwithbackend.solution.solution.domain.SolutionEntity;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "PAYMENT_EVENT_ENTITY",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"vendor_seq", "consumer_seq", "solution_seq"})
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@SuperBuilder
public class PaymentEventEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_event_seq")
    private Long paymentEventSeq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_seq", nullable = false)
    private VendorEntity vendorEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer_seq", nullable = false)
    private ConsumerEntity customerEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solution_seq", nullable = false)
    private SolutionEntity solutionEntity;

    @Column(name = "payment_event_name", nullable = false)
    private String paymentEventName;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "sell_type", nullable = false)
    private SELL_TYPE sellType;

    @Column(name = "duration", nullable = false)
    private Long duration;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_event_status", nullable = false)
    private PAYMENT_EVENT_STATUS paymentEventStatus;

    @Column(name = "payment_completed_at")
    private LocalDateTime paymentCompletedAt;

    @Column(name = "development_completed_at")
    private LocalDateTime developmentCompletedAt;

    @Column(name = "auto_confirm_scheduled_at")
    private LocalDateTime autoConfirmScheduledAt;

    public PaymentEventEntity updateDevelopmentCompletedAt() {
        LocalDateTime now = LocalDateTime.now();

        this.developmentCompletedAt = now;
        this.autoConfirmScheduledAt = now.plusDays(7);
        this.paymentEventStatus = PAYMENT_EVENT_STATUS.DEVELOPED;

        return this;
    }

    public PaymentEventEntity updatePaymentEventStatus(PAYMENT_EVENT_STATUS paymentEventStatus) {
        this.paymentEventStatus = paymentEventStatus;

        return this;
    }
}
