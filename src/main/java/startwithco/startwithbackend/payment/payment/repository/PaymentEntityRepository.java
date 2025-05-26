package startwithco.startwithbackend.payment.payment.repository;

import startwithco.startwithbackend.payment.payment.domain.PaymentEntity;

import java.util.Optional;

public interface PaymentEntityRepository {
    PaymentEntity savePaymentEntity(PaymentEntity paymentEntity);

    Optional<PaymentEntity> findByOrderId(String orderId);

    Optional<PaymentEntity> findByPaymentEventSeq(Long paymentEventSeq);
}
