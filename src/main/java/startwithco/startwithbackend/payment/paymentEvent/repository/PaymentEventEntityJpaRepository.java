package startwithco.startwithbackend.payment.paymentEvent.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.payment.paymentEvent.domain.PaymentEventEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentEventEntityJpaRepository extends JpaRepository<PaymentEventEntity, Long> {
    @Query("""
                SELECT pe
                FROM PaymentEventEntity pe
                WHERE pe.paymentEventSeq = :paymentEventSeq
                  AND pe.isCanceled = false
            """)
    Optional<PaymentEventEntity> findByPaymentEventSeq(@Param("paymentEventSeq") Long paymentEventSeq);

    @Query("""
                SELECT pe, p
                FROM PaymentEventEntity pe
                LEFT JOIN PaymentEntity p ON p.paymentEventEntity = pe
                WHERE pe.consumerEntity.consumerSeq = :consumerSeq
                  AND pe.vendorEntity.vendorSeq = :vendorSeq
            """)
    List<Object[]> findAllByConsumerSeqAndVendorSeq(
            @Param("consumerSeq") Long consumerSeq,
            @Param("vendorSeq") Long vendorSeq
    );
}
