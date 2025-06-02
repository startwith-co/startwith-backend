package startwithco.startwithbackend.payment.payment.repository;

import startwithco.startwithbackend.payment.payment.domain.PaymentEntity;

import java.util.List;
import java.util.Optional;

public interface PaymentEntityRepository {
    PaymentEntity savePaymentEntity(PaymentEntity paymentEntity);

    boolean canApproveTossPayment(String orderId, Long paymentEventSeq);

    Optional<PaymentEntity> findBySecret(String secret);

    Optional<PaymentEntity> findByOrderId(String orderId);
}
