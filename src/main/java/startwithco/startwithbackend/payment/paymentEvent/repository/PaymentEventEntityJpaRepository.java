package startwithco.startwithbackend.payment.paymentEvent.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.payment.paymentEvent.domain.PaymentEventEntity;

import java.util.Optional;

@Repository
public interface PaymentEventEntityJpaRepository extends JpaRepository<PaymentEventEntity, Long> {
    @Query("""
            SELECT pe
            FROM PaymentEventEntity pe
            JOIN FETCH pe.solutionEntity s
            WHERE pe.paymentEventSeq = :paymentEventSeq
            """)
    Optional<PaymentEventEntity> findByPaymentEventSeq(@Param("paymentEventSeq") Long paymentEventSeq);

    @Query("""
            SELECT pe
            FROM PaymentEventEntity pe
            WHERE pe.paymentEventUniqueType = :paymentEventUniqueType
            """)
    Optional<PaymentEventEntity> findByPaymentEventUniqueType(@Param("paymentEventUniqueType") String paymentEventUniqueType);
}
