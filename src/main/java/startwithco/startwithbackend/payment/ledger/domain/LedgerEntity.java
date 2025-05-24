package startwithco.startwithbackend.payment.ledger.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import startwithco.startwithbackend.base.BaseTimeEntity;
import startwithco.startwithbackend.payment.ledger.util.ENTRY_TYPE;

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

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ENTRY_TYPE entryType;

    @Column(name = "debit", nullable = false, updatable = false)
    private Long debit;

    @Column(name = "credit", nullable = false, updatable = false)
    private Long credit;
}
