package startwithco.startwithbackend.payment.payment.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.payment.payment.domain.PaymentEntity;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentEntityRepositoryImpl implements PaymentEntityRepository {
    private final PaymentEntityJpaRepository repository;

    @Override
    public PaymentEntity savePaymentEntity(PaymentEntity paymentEntity) {
        return repository.save(paymentEntity);
    }

    @Override
    public boolean canApproveTossPayment(String orderId, Long paymentEventSeq) {
        return repository.canApproveTossPayment(orderId, paymentEventSeq);
    }

    @Override
    public Optional<PaymentEntity> findSUCCESSByPaymentEventSeq(Long paymentEventSeq) {
        return repository.findSUCCESSByPaymentEventSeq(paymentEventSeq);
    }

    @Override
    public boolean canSavePaymentEntity(Long paymentEventSeq) {
        return repository.canSavePaymentEntity(paymentEventSeq);
    }

    @Override
    public Optional<PaymentEntity> findINPROGRESSByPaymentEventSeq(Long paymentEventSeq) {
        return repository.findINPROGRESSByPaymentEventSeq(paymentEventSeq);
    }

    @Override
    public Optional<PaymentEntity> findBySecret(String secret) {
        return repository.findBySecret(secret);
    }

    @Override
    public Optional<PaymentEntity> findByOrderId(String orderId) {
        return repository.findByOrderId(orderId);
    }
}