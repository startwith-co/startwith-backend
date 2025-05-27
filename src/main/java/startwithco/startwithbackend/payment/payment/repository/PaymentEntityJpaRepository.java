package startwithco.startwithbackend.payment.payment.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.payment.payment.domain.PaymentEntity;

import java.util.Optional;

@Repository
public interface PaymentEntityJpaRepository extends JpaRepository<PaymentEntity, Long> {
    @Query("SELECT pe FROM PaymentEntity pe WHERE pe.orderId = :orderId")
    Optional<PaymentEntity> findByOrderId(@Param("orderId") String orderId);

    @Query("""
            SELECT CASE
                     WHEN COUNT(p) > 0 THEN false
                     ELSE true
                   END
            FROM PaymentEntity p
            WHERE p.paymentEventEntity.paymentEventSeq = :paymentEventSeq
              AND p.orderId = :orderId
              AND p.paymentStatus IN ('SUCCESS', 'FAILURE')
        """)
    boolean canApproveTossPayment(
            @Param("orderId") String orderId,
            @Param("paymentEventSeq") Long paymentEventSeq
    );
}
