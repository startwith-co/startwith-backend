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
    public Optional<PaymentEntity> findByOrderId(String orderId) {
        return repository.findByOrderId(orderId);
    }
}
