package startwithco.startwithbackend.payment.leger.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import startwithco.startwithbackend.b2b.consumer.domain.ConsumerEntity;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;
import startwithco.startwithbackend.base.BaseTimeEntity;

@Entity
@Table(name = "LEDGER_ENTITY")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@SuperBuilder
public class LedgerEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ledger_seq")
    private Long ledgerSeq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_seq")
    private VendorEntity vendorEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer_seq")
    private ConsumerEntity consumerEntity;

    @Column(name = "amont", nullable = false)
    private Long amount;

    @Column(name = "toss_amount", nullable = false)
    private Long tossAmount;

    @Column(name = "settlement_amount", nullable = false)
    private Long settlementAmount;

    @Column(name = "is_settled", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isSettled = false;
}
