package startwithco.startwithbackend.payment.snapshot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.payment.snapshot.entity.TossPaymentDailySnapshotEntity;

import java.util.Optional;

@Repository
public interface TossPaymentDailySnapshotEntityJpaRepository extends JpaRepository<TossPaymentDailySnapshotEntity, Long> {
    @Query("""
            SELECT snap
            FROM TossPaymentDailySnapshotEntity snap
            WHERE snap.orderId = :orderId
            """)
    Optional<TossPaymentDailySnapshotEntity> findByOrderId(@Param("orderId") String orderId);
}
