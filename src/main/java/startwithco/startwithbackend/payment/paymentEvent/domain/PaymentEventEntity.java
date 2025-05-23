package startwithco.startwithbackend.payment.paymentEvent.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import startwithco.startwithbackend.b2b.consumer.domain.ConsumerEntity;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;
import startwithco.startwithbackend.base.BaseTimeEntity;
import startwithco.startwithbackend.util.SELL_TYPE;

@Entity
@Table(name = "PAYMENT_EVENT_ENTITY")
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

    @Column(name = "solution_seq", nullable = false)
    private Long solutionSeq;

    @Column(name = "solution_name", nullable = false)
    private String solutionName;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "sell_type", nullable = false)
    private SELL_TYPE sellType;

    @Column(name = "duration", nullable = false)
    private Long duration;
}
