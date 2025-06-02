package startwithco.startwithbackend.payment.paymentEvent.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import startwithco.startwithbackend.b2b.consumer.domain.ConsumerEntity;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;
import startwithco.startwithbackend.base.BaseTimeEntity;
import startwithco.startwithbackend.solution.solution.domain.SolutionEntity;

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
    private ConsumerEntity consumerEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solution_seq", nullable = false)
    private SolutionEntity solutionEntity;

    @Column(name = "payment_event_name", nullable = false)
    private String paymentEventName;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "tax", nullable = false)
    private Long tax;

    @Column(name = "actual_amount", nullable = false)
    private Long actualAmount;

    @Column(name = "contract_confirmation_url", nullable = false)
    private String contractConfirmationUrl;

    @Column(name = "refund_policy_url", nullable = false)
    private String refundPolicyUrl;
}