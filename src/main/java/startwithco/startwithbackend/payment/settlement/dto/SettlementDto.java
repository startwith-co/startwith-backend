package startwithco.startwithbackend.payment.settlement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import startwithco.startwithbackend.payment.payment.util.METHOD;
import startwithco.startwithbackend.payment.payment.util.PAYMENT_STATUS;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SettlementDto {
    LocalDateTime paymentCompletedAt;
    String orderId;
    PAYMENT_STATUS paymentStatus;
    METHOD method;
    Long amount;
    Long payOutAmount;
    Long settlementAmount;
    String consumerName;
    String vendorName;
    String accountNumber;
    String bank;
    String solutionName;
    boolean isSettled;
}
