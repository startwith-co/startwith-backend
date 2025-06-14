package startwithco.startwithbackend.payment.payment.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.payment.payment.domain.PaymentEntity;
import startwithco.startwithbackend.payment.paymentEvent.domain.PaymentEventEntity;

import java.util.Optional;

@Repository
public interface PaymentEntityJpaRepository extends JpaRepository<PaymentEntity, Long> {
    @Query("""
            SELECT CASE
                     WHEN COUNT(p) = 0 THEN true
                     ELSE false
                   END
            FROM PaymentEntity p
            WHERE p.orderId = :orderId
              AND p.paymentEventEntity.paymentEventSeq = :paymentEventSeq
              AND p.paymentStatus = 'IN_PROGRESS'
            """)
    boolean canApproveTossPayment(@Param("orderId") String orderId, Long paymentEventSeq);

    @Query("""
            SELECT p
            FROM PaymentEntity p
            WHERE p.secret = :secret
            """)
    Optional<PaymentEntity> findBySecret(@Param("secret") String secret);

    @Query("""
            SELECT p
            FROM PaymentEntity p
            WHERE p.orderId = :orderId
            """)
    Optional<PaymentEntity> findByOrderId(@Param("orderId") String orderId);

    @Query("""
            SELECT COUNT(p)
            FROM PaymentEntity p
            WHERE p.paymentEventEntity.vendorEntity.vendorSeq = :vendorSeq
              AND p.paymentStatus = 'DONE'
            """)
    Long countDONEStatusByVendorSeq(@Param("vendorSeq") Long vendorSeq);

    @Query("""
            SELECT COUNT(p)
            FROM PaymentEntity p
            WHERE p.paymentEventEntity.vendorEntity.vendorSeq = :vendorSeq
              AND p.paymentStatus = 'SETTLED'
            """)
    Long countSETTLEDStatusByVendorSeq(Long vendorSeq);

    @Query("""
            SELECT p
            FROM PaymentEntity p
            JOIN FETCH p.paymentEventEntity pe
            JOIN FETCH p.paymentEventEntity.solutionEntity s
            WHERE pe.paymentEventUniqueType = :paymentEventUniqueType
            """)
    Optional<PaymentEntity> findByPaymentEventUniqueType(@Param("paymentEventUniqueType") String paymentEventUniqueType);
}
