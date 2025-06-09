package startwithco.startwithbackend.payment.payment.repository;

import startwithco.startwithbackend.payment.payment.domain.PaymentEntity;

import java.util.List;
import java.util.Optional;

public interface PaymentEntityRepository {
    PaymentEntity savePaymentEntity(PaymentEntity paymentEntity);

    boolean canApproveTossPayment(String orderId, Long paymentEventSeq);

    Optional<PaymentEntity> findBySecret(String secret);

    Optional<PaymentEntity> findByOrderId(String orderId);

    List<PaymentEntity> findAllByConsumerSeqAndPaymentStatus(Long consumerSeq, String paymentStatus, int start, int end);

    Long countDONEStatusByVendorSeq(Long vendorSeq);

    Long countSETTLEDStatusByVendorSeq(Long vendorSeq);

    List<PaymentEntity> findAllByVendorSeqAndPaymentStatus(Long vendorSeq, String paymentStatus, int start, int end);

    List<PaymentEntity> findAll(int start, int end);

    Optional<PaymentEntity> findByPaymentEventUniqueType(String paymentEventUniqueType);
}
