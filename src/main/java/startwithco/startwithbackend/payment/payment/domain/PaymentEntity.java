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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_event_seq", nullable = false)
    private PaymentEventEntity paymentEventEntity;

    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;

    @Column(name = "payment_key", nullable = false, unique = true)
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

    @Column(name = "due_date", nullable = true)
    private LocalDateTime dueDate;

    public void updateCardDONEStatus(LocalDateTime paymentCompletedAt) {
        this.paymentStatus = PAYMENT_STATUS.DONE;
        this.method = METHOD.CARD;
        this.secret = null;
        this.paymentCompletedAt = paymentCompletedAt;
        this.autoConfirmScheduledAt = paymentCompletedAt.plusDays(7);
        this.dueDate = paymentCompletedAt.plusDays(1);
    }

    public void updateVirtualWAITING_FOR_DEPOSITStatus(LocalDateTime requestedAt, String secret) {
        this.paymentStatus = PAYMENT_STATUS.WAITING_FOR_DEPOSIT;
        this.method = METHOD.VIRTUAL_ACCOUNT;
        this.secret = secret;
        this.paymentCompletedAt = null;
        this.autoConfirmScheduledAt = null;
        this.dueDate = requestedAt.plusDays(1);
    }

    public void updateEasyPayDONEStatus(LocalDateTime paymentCompletedAt) {
        this.paymentStatus = PAYMENT_STATUS.DONE;
        this.method = METHOD.EASY_PAY;
        this.secret = null;
        this.paymentCompletedAt = paymentCompletedAt;
        this.autoConfirmScheduledAt = paymentCompletedAt.plusDays(7);
        this.dueDate = paymentCompletedAt.plusDays(1);
    }

    public void updateVirtualDONEStatus(LocalDateTime paymentCompletedAt) {
        this.paymentStatus = PAYMENT_STATUS.DONE;
        this.method = METHOD.VIRTUAL_ACCOUNT;
        this.paymentCompletedAt = paymentCompletedAt;
        this.autoConfirmScheduledAt = paymentCompletedAt.plusDays(7);
    }

    public void updateFAILUREStatus() {
        this.paymentStatus = PAYMENT_STATUS.FAILED;
        this.method = null;
        this.secret = null;
        this.paymentCompletedAt = null;
        this.autoConfirmScheduledAt = null;
        this.dueDate = null;
    }

    public void updateCANCELStatus(LocalDateTime paymentCompletedAt) {
        this.paymentStatus = PAYMENT_STATUS.CANCELLED;
        this.paymentCompletedAt = paymentCompletedAt;
    }

    public void updateSETTLEDStatus() {
        this.paymentStatus = PAYMENT_STATUS.SETTLED;
        this.autoConfirmScheduledAt = LocalDateTime.now();
    }

    public void updateCANCELDStatus() {
        this.paymentStatus = PAYMENT_STATUS.DONE;
        this.autoConfirmScheduledAt = LocalDateTime.now().plusDays(7);
    }
}
