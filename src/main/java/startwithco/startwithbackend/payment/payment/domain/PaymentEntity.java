package startwithco.startwithbackend.payment.payment.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
@Setter
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

    @Column(name = "due_date", nullable = true)
    private LocalDateTime dueDate;

    public void markAsSettled() {
        this.paymentStatus = PAYMENT_STATUS.SETTLED;
    }

    public void updateStatusFromWebhook(String status, LocalDateTime completedAt, String methodStr) {
        if (methodStr != null && this.method == null) {
            switch (methodStr) {
                case "카드" -> this.method = METHOD.CARD;
                case "가상계좌" -> this.method = METHOD.VIRTUAL_ACCOUNT;
                case "간편결제" -> this.method = METHOD.EASY_PAY;
            }
        }

        switch (status) {
            case "EXPIRED" -> {
                if (this.method == METHOD.CARD || this.method == METHOD.EASY_PAY) {
                    this.paymentStatus = PAYMENT_STATUS.EXPIRED;
                    this.secret = null;
                }
            }
            case "ABORTED" -> {
                if (this.method == METHOD.CARD || this.method == METHOD.EASY_PAY) {
                    this.paymentStatus = PAYMENT_STATUS.ABORTED;
                    this.secret = null;
                }
            }
            case "DONE" -> {
                this.paymentStatus = PAYMENT_STATUS.DONE;
                if (completedAt != null) {
                    this.paymentCompletedAt = completedAt;
                    if (this.method == METHOD.CARD || this.method == METHOD.EASY_PAY) {
                        this.dueDate = completedAt.plusDays(1);
                    }
                }
                if (this.method != METHOD.VIRTUAL_ACCOUNT) {
                    this.secret = null;
                }
            }
            case "CANCELED" -> {
                this.paymentStatus = PAYMENT_STATUS.CANCELED;
                if (completedAt != null) {
                    this.paymentCompletedAt = completedAt;
                }
            }
            case "PARTIAL_CANCELED" -> {
                this.paymentStatus = PAYMENT_STATUS.PARTIAL_CANCELED;
                if (completedAt != null) {
                    this.paymentCompletedAt = completedAt;
                }
            }
            case "WAITING_FOR_DEPOSIT" -> {
                if (this.method == METHOD.VIRTUAL_ACCOUNT) {
                    this.paymentStatus = PAYMENT_STATUS.WAITING_FOR_DEPOSIT;
                }
            }
            case "READY", "IN_PROGRESS" -> {
                // 상태 변경 없음
            }
        }
    }

    public void updateCancelStatusFromWebhook(String cancelStatus, LocalDateTime canceledAt) {
        switch (cancelStatus) {
            case "IN_PROGRESS" -> {
                // 취소 진행 중 - 상태 변경 없음
            }
            case "DONE" -> {
                this.paymentStatus = PAYMENT_STATUS.CANCELED;
                if (canceledAt != null) {
                    this.paymentCompletedAt = canceledAt;
                }
            }
            case "ABORTED" -> {
                // 취소 실패 - 상태는 유지 (이전 상태 그대로)
            }
        }
    }
}
