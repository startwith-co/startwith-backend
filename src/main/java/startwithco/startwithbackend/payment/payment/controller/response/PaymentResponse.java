package startwithco.startwithbackend.payment.payment.controller.response;

import java.time.LocalDateTime;

public class PaymentResponse {
    public record TossPaymentApprovalResponse(
            String orderId,
            String orderName,
            String paymentKey,
            String method,
            Integer totalAmount,
            LocalDateTime approvedAt,
            String receiptUrl
    ) {}
}
