package startwithco.startwithbackend.payment.paymentEvent.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.payment.paymentEvent.domain.PaymentEventEntity;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentEventEntityRepositoryImpl implements PaymentEventEntityRepository {
    private final PaymentEventEntityJpaRepository repository;

    @Override
    public PaymentEventEntity savePaymentEventEntity(PaymentEventEntity paymentEventEntity) {
        return repository.save(paymentEventEntity);
    }

    @Override
    public Optional<PaymentEventEntity> findByPaymentEventSeq(Long paymentEventSeq) {
        return repository.findByPaymentEventSeq(paymentEventSeq);
    }

    @Override
    public boolean canSavePaymentEventEntity(Long consumerSeq, Long vendorSeq, Long solutionSeq) {
        return repository.canSavePaymentEventEntity(consumerSeq, vendorSeq, solutionSeq);
    }

    @Override
    public Long countREQUESTEDPaymentEntityByVendorSeq(Long vendorSeq) {
        return repository.countREQUESTEDPaymentEntityByVendorSeq(vendorSeq);
    }

    @Override
    public Long countCONFIRMEDPaymentEntityByVendorSeq(Long vendorSeq) {
        return repository.countCONFIRMEDPaymentEntityByVendorSeq(vendorSeq);
    }

    @Override
    public Long countSETTLEDPaymentEntityByVendorSeq(Long vendorSeq) {
        return repository.countSETTLEDPaymentEntityByVendorSeq(vendorSeq);
    }

    @Override
    public List<PaymentEventEntity> findAllByConsumerSeq(Long consumerSeq) {
        return repository.findAllByConsumerSeq(consumerSeq);
    }
}
