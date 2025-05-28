package startwithco.startwithbackend.payment.payment.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.payment.payment.domain.PaymentEntity;

import java.util.Optional;

@Repository
public interface PaymentEntityJpaRepository extends JpaRepository<PaymentEntity, Long> {
    @Query("""
                SELECT CASE
                         WHEN COUNT(p) > 0 THEN false
                         ELSE true
                       END
                FROM PaymentEntity p
                WHERE p.paymentEventEntity.orderId = :orderId
                  AND p.paymentEventEntity.paymentEventSeq = :paymentEventSeq
                  AND p.paymentStatus IN ('SUCCESS', 'FAILURE')
            """)
    boolean canApproveTossPayment(
            @Param("orderId") String orderId,
            @Param("paymentEventSeq") Long paymentEventSeq
    );

    @Query("""
            SELECT p
            FROM PaymentEntity p
            WHERE p.paymentEventEntity.paymentEventSeq = :paymentEventSeq
              AND p.paymentStatus = 'SUCCESS'
            """)
    Optional<PaymentEntity> findSUCCESSByPaymentEventSeq(@Param("paymentEventSeq") Long paymentEventSeq);

    @Query("""
            SELECT CASE
                     WHEN COUNT(p) = 0 THEN true
                     ELSE false
                   END
            FROM PaymentEntity p
            WHERE p.paymentEventEntity.paymentEventSeq = :paymentEventSeq
              AND p.paymentStatus = 'IN_PROGRESS'
            """)
    boolean canSavePaymentEntity(@Param("paymentEventSeq") Long paymentEventSeq);

    @Query("""
            SELECT p
            FROM PaymentEntity p
            WHERE p.paymentEventEntity.paymentEventSeq = :paymentEventSeq
              AND p.paymentStatus = 'IN_PROGRESS'
            """)
    Optional<PaymentEntity> findINPROGRESSByPaymentEventSeq(@Param("paymentEventSeq") Long paymentEventSeq);
}
