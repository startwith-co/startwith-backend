package startwithco.startwithbackend.payment.paymentEvent.repository;

import startwithco.startwithbackend.payment.paymentEvent.domain.PaymentEventEntity;

public interface PaymentEventEntityRepository {
    PaymentEventEntity savePaymentEventEntity(PaymentEventEntity paymentEventEntity);
}
