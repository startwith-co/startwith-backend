package startwithco.startwithbackend.payment.paymentEvent.repository;

import startwithco.startwithbackend.payment.paymentEvent.domain.PaymentEventEntity;

import java.util.Optional;

public interface PaymentEventEntityRepository {
    PaymentEventEntity savePaymentEventEntity(PaymentEventEntity paymentEventEntity);

    Optional<PaymentEventEntity> findByPaymentEventSeq(Long paymentEventSeq);

    Long countDEVELOPINGByConsumerSeq(Long consumerSeq);

    Long countDEVELOPEDByConsumerSeq(Long consumerSeq);

    Long countCONFIRMEDByConsumerSeq(Long consumerSeq);
}
