package startwithco.startwithbackend.payment.paymentEvent.repository;

import startwithco.startwithbackend.payment.paymentEvent.domain.PaymentEventEntity;

import java.util.Optional;

public interface PaymentEventEntityRepository {
    PaymentEventEntity savePaymentEventEntity(PaymentEventEntity paymentEventEntity);

    Optional<PaymentEventEntity> findByPaymentEventSeq(Long paymentEventSeq);

    boolean canSavePaymentEventEntity(Long consumerSeq, Long vendorSeq, Long solutionSeq);

    Long countREQUESTEDPaymentEntityByVendorSeq(Long vendorSeq);

    Long countCONFIRMEDPaymentEntityByVendorSeq(Long vendorSeq);

    Long countSETTLEDPaymentEntityByVendorSeq(Long vendorSeq);
}
