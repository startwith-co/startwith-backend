package startwithco.startwithbackend.payment.paymentEvent.controller.response;

import startwithco.startwithbackend.payment.paymentEvent.util.PAYMENT_EVENT_STATUS;
import startwithco.startwithbackend.solution.solution.util.CATEGORY;

public class PaymentEventResponse {
    public record SavePaymentEventEntityResponse(
            Long paymentEventSeq
    ) {

    }

    public record GetREQUESTEDPaymentEventEntityResponse(
            Long paymentEventSeq,
            String paymentEventName,
            CATEGORY category,
            Long amount,
            PAYMENT_EVENT_STATUS paymentEventStatus,
            String contractConfirmationUrl,
            String refundPolicyUrl,
            String orderId
    ) {

    }

    public record GetCONFIRMEDPaymentEventEntityResponse(
            Long paymentEventSeq,
            String paymentEventName,
            CATEGORY category,
            Long amount,
            PAYMENT_EVENT_STATUS paymentEventStatus
    ) {

    }

    public record GetPaymentEventEntityOrderResponse(
            // 주문 내역
            Long paymentEventSeq,
            String orderId,
            String paymentEventName,
            CATEGORY category,
            String vendorName,
            String vendorBannerImageUrl,
            String representImageUrl,

            // 총 결제 금액
            Long amount,
            Long tax,
            Long actualAmount,

            // 주문자 정보
            Long consumerSeq,
            String consumerName,
            String phoneNumber,
            String email
    ) {

    }
}
