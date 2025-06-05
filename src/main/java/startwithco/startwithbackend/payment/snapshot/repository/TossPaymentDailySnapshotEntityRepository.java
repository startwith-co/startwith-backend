package startwithco.startwithbackend.payment.snapshot.repository;

import startwithco.startwithbackend.payment.snapshot.entity.TossPaymentDailySnapshotEntity;

import java.util.Optional;

public interface TossPaymentDailySnapshotEntityRepository {
    Optional<TossPaymentDailySnapshotEntity> findByOrderId(String orderId);
}
