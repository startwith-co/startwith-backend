package startwithco.startwithbackend.payment.payment.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import startwithco.startwithbackend.base.BaseTimeEntity;
import startwithco.startwithbackend.payment.payment.util.METHOD;
import startwithco.startwithbackend.payment.payment.util.PAYMENT_STATUS;
import startwithco.startwithbackend.payment.paymentEvent.domain.PaymentEventEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "PAYMENT_ENTITY")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@SuperBuilder
public class PaymentEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_seq")
    private Long paymentSeq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_event_seq", nullable = false)
    private PaymentEventEntity paymentEventEntity;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "payment_key", nullable = false)
    private String paymentKey;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PAYMENT_STATUS paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = true)
    private METHOD method;

    @Column(name = "secret", nullable = true)
    private String secret;

    @Column(name = "payment_completed_at", nullable = true)
    private LocalDateTime paymentCompletedAt;

    @Column(name = "auto_confirm_scheduled_at", nullable = true)
    private LocalDateTime autoConfirmScheduledAt;

    public void updateFailureStatus() {
        this.paymentStatus = PAYMENT_STATUS.FAILURE;
        this.paymentCompletedAt = null;
    }

    public void updateSuccessStatus(LocalDateTime paymentCompletedAt) {
        this.paymentStatus = PAYMENT_STATUS.SUCCESS;
        this.paymentCompletedAt = paymentCompletedAt;
        this.autoConfirmScheduledAt = paymentCompletedAt.plusDays(7);
    }

    public void updatePaymentStatus(PAYMENT_STATUS paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void updateMethod(METHOD method) {
        this.method = method;
    }

    public void updateSecret(String secret) {
        this.secret = secret;
    }
}
