package startwithco.startwithbackend.payment.paymentEvent.controller.response;

public class PaymentEventResponse {
    public record SavePaymentEventEntityResponse(
            Long paymentEventSeq
    ) {

    }
}
