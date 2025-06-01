package startwithco.startwithbackend.payment.payment.controller.response;

import startwithco.startwithbackend.payment.payment.util.METHOD;
import startwithco.startwithbackend.payment.payment.util.PAYMENT_STATUS;
import startwithco.startwithbackend.payment.paymentEvent.util.PAYMENT_EVENT_STATUS;

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
            String receiptUrl
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
            String receiptUrl
    ) {

    }

    public record GetTossPaymentApprovalResponse(
            String orderId,
            Long amount,
            PAYMENT_EVENT_STATUS paymentEventStatus,
            PAYMENT_STATUS paymentStatus,
            METHOD method
    ) {

    }
}
