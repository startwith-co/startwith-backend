package startwithco.startwithbackend.payment.payment.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import startwithco.startwithbackend.base.BaseTimeEntity;
import startwithco.startwithbackend.payment.paymentEvent.domain.PaymentEventEntity;
import startwithco.startwithbackend.payment.payment.util.STATUS;

import java.math.BigDecimal;

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
    @Column(name = "status", nullable = false)
    private STATUS status;

    public PaymentEntity updateStatus(STATUS status) {
        this.status = status;

        return this;
    }
}
