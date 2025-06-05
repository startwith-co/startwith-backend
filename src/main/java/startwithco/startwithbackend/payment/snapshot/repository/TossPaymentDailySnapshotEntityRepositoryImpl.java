package startwithco.startwithbackend.payment.snapshot.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.payment.snapshot.entity.TossPaymentDailySnapshotEntity;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TossPaymentDailySnapshotEntityRepositoryImpl implements TossPaymentDailySnapshotEntityRepository {
    private final TossPaymentDailySnapshotEntityJpaRepository repository;

    @Override
    public TossPaymentDailySnapshotEntity saveTossPaymentDailySnapshot(TossPaymentDailySnapshotEntity tossPaymentDailySnapshotEntity) {
        return repository.save(tossPaymentDailySnapshotEntity);
    }

    @Override
    public Optional<TossPaymentDailySnapshotEntity> findByOrderId(String orderId) {
        return repository.findByOrderId(orderId);
    }
}
