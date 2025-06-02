package startwithco.startwithbackend.payment.paymentEvent.repository;

import startwithco.startwithbackend.payment.paymentEvent.domain.PaymentEventEntity;

import java.util.List;
import java.util.Optional;

public interface PaymentEventEntityRepository {
    PaymentEventEntity savePaymentEventEntity(PaymentEventEntity paymentEventEntity);

    Optional<PaymentEventEntity> findByPaymentEventSeq(Long paymentEventSeq);

    List<Object[]> findAllByConsumerSeqAndVendorSeq(Long consumerSeq, Long vendorSeq);
}
