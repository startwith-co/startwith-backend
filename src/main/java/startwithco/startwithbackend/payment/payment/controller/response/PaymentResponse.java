package startwithco.startwithbackend.payment.payment.controller.response;

public class PaymentResponse {
    public record TossPaymentApprovalResponse(
            String orderId,
            String orderName,
            String paymentKey,
            String method,
            Integer totalAmount,
            String approvedAt,
            String receiptUrl
    ) {

    }
}
