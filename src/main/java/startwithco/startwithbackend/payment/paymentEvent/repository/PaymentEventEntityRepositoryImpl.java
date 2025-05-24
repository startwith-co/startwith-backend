package startwithco.startwithbackend.payment.paymentEvent.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.payment.paymentEvent.domain.PaymentEventEntity;

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
}
