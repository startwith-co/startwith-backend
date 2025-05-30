package startwithco.startwithbackend.payment.snapshot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import startwithco.startwithbackend.base.BaseTimeEntity;

@Entity
@Table(
        name = "TOSS_PAYMENT_DAILY_SNAPSHOT_ENTITY"
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@SuperBuilder
public class TossPaymentDailySnapshotEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "toss_payment_daily_snapshot_seq")
    private Long tossPaymentDailySnapshotSeq;

    @Column(name = "payment_key", nullable = false)
    private String paymentKey;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "method", nullable = false)
    private String method;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Column(name = "balance_amount", nullable = false)
    private Long balanceAmount;         // 남은 금액

    @Column(name = "supplied_amount", nullable = false)
    private Long suppliedAmount;        // 공급가액

    @Column(name = "vat", nullable = false)
    private Long vat;                   // 부가세

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "receipt_url", nullable = false)
    private String receiptUrl;
}
