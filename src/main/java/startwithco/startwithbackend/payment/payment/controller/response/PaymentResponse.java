package startwithco.startwithbackend.payment.payment.controller.response;

import startwithco.startwithbackend.solution.solution.util.CATEGORY;

import java.time.LocalDateTime;

public class PaymentResponse {
    public record TossCardPaymentApprovalResponse(
            String orderId,
            String orderName,
            String paymentKey,
            String method,
            Integer totalAmount,
            LocalDateTime approvedAt,
            String cardCompany,
            String cardNumber,
            String cardType,
            String receiptUrl,
            CATEGORY category
    ) {

    }

    public record TossVirtualAccountPaymentResponse(
            String orderId,
            String orderName,
            String paymentKey,
            String method,
            Integer totalAmount,
            LocalDateTime requestedAt,
            String virtualAccountNumber,
            String bankCode,
            String customerName,
            LocalDateTime dueDate,
            String cashReceiptUrl,
            String secret,
            String receiptUrl,
            CATEGORY category
    ) {

    }
}
