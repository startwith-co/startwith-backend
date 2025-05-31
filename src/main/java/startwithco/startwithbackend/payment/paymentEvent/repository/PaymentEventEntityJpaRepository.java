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
            """)
    Optional<PaymentEventEntity> findByPaymentEventSeq(@Param("paymentEventSeq") Long paymentEventSeq);

    @Query("""
                SELECT CASE
                         WHEN COUNT(pe) = 0 THEN true
                         ELSE false
                         END
                FROM PaymentEventEntity pe
                WHERE pe.consumerEntity.consumerSeq = :consumerSeq
                  AND pe.vendorEntity.vendorSeq = :vendorSeq
                  AND pe.solutionEntity.solutionSeq = :solutionSeq
                  AND pe.paymentEventStatus = 'REQUESTED'
            """)
    boolean canSavePaymentEventEntity(
            @Param("consumerSeq") Long consumerSeq,
            @Param("vendorSeq") Long vendorSeq,
            @Param("solutionSeq") Long solutionSeq
    );

    @Query("""
                SELECT COUNT(*)
                FROM PaymentEventEntity pe
                WHERE pe.vendorEntity.vendorSeq = :vendorSeq
                  AND pe.paymentEventStatus = 'REQUESTED'
            """)
    Long countREQUESTEDPaymentEntityByVendorSeq(@Param("vendorSeq") Long vendorSeq);

    @Query("""
                SELECT COUNT(*)
                FROM PaymentEventEntity pe
                WHERE pe.vendorEntity.vendorSeq = :vendorSeq
                  AND pe.paymentEventStatus = 'CONFIRMED'
            """)
    Long countCONFIRMEDPaymentEntityByVendorSeq(@Param("vendorSeq") Long vendorSeq);

    @Query("""
                SELECT COUNT(*)
                FROM PaymentEventEntity pe
                WHERE pe.vendorEntity.vendorSeq = :vendorSeq
                  AND pe.paymentEventStatus = 'SETTLED'
            """)
    Long countSETTLEDPaymentEntityByVendorSeq(@Param("vendorSeq") Long vendorSeq);

    @Query("""
                SELECT pe
                FROM PaymentEventEntity pe
                WHERE pe.consumerEntity.consumerSeq = :consumerSeq
                  AND (pe.paymentEventStatus = 'CONFIRMED' OR pe.paymentEventStatus = 'SETTLED')
            """)
    List<PaymentEventEntity> findAllByConsumerSeq(@Param("consumerSeq") Long consumerSeq);
}
