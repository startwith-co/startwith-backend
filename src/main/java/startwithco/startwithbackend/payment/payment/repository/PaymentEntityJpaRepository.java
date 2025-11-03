package startwithco.startwithbackend.payment.payment.repository;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.payment.payment.domain.PaymentEntity;

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
            """)
    boolean canApproveTossPayment(@Param("orderId") String orderId, @Param("paymentEventSeq") Long paymentEventSeq);

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
            SELECT p
            FROM PaymentEntity p
            WHERE p.paymentKey = :paymentKey
            """)
    Optional<PaymentEntity> findByPaymentKey(@Param("paymentKey") String paymentKey);

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
    Long countSETTLEDStatusByVendorSeq(@Param("vendorSeq") Long vendorSeq);

    @Query("""
            SELECT p
            FROM PaymentEntity p
            JOIN FETCH p.paymentEventEntity pe
            JOIN FETCH p.paymentEventEntity.solutionEntity s
            WHERE pe.paymentEventUniqueType = :paymentEventUniqueType
            """)
    Optional<PaymentEntity> findByPaymentEventUniqueType(@Param("paymentEventUniqueType") String paymentEventUniqueType);
}
