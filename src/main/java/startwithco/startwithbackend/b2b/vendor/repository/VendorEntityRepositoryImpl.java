package startwithco.startwithbackend.b2b.vendor.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.b2b.consumer.domain.QConsumerEntity;
import startwithco.startwithbackend.b2b.vendor.domain.QVendorEntity;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;
import startwithco.startwithbackend.payment.leger.domain.QLedgerEntity;
import startwithco.startwithbackend.payment.payment.domain.PaymentEntity;
import startwithco.startwithbackend.payment.payment.domain.QPaymentEntity;
import startwithco.startwithbackend.payment.payment.util.PAYMENT_STATUS;
import startwithco.startwithbackend.payment.paymentEvent.domain.PaymentEventEntity;
import startwithco.startwithbackend.payment.paymentEvent.domain.QPaymentEventEntity;
import startwithco.startwithbackend.payment.paymentEvent.util.PAYMENT_EVENT_STATUS;
import startwithco.startwithbackend.solution.solution.domain.QSolutionEntity;

import java.util.List;
import java.util.Optional;

import static startwithco.startwithbackend.b2b.vendor.controller.response.VendorResponse.*;

@Repository
@RequiredArgsConstructor
public class VendorEntityRepositoryImpl implements VendorEntityRepository {
    private final VendorEntityJpaRepository repository;
    private final JPAQueryFactory queryFactory;

    public Optional<VendorEntity> findByVendorSeq(Long vendorSeq) {
        return repository.findByVendorSeq(vendorSeq);
    }

    @Override
    public Optional<VendorEntity> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Override
    public void save(VendorEntity vendorEntity) {
        repository.save(vendorEntity);
    }

    @Override
    public List<GetVendorSettlementManagementProgressResponse> getVendorSettlementManagementProgress(Long vendorSeq, String paymentEventStatus, int start, int end) {
        QPaymentEventEntity qPaymentEventEntity = QPaymentEventEntity.paymentEventEntity;
        QSolutionEntity qSolutionEntity = QSolutionEntity.solutionEntity;
        QPaymentEntity qPaymentEntity = QPaymentEntity.paymentEntity;
        QLedgerEntity qLedgerEntity = QLedgerEntity.ledgerEntity;
        QConsumerEntity qConsumerEntity = QConsumerEntity.consumerEntity;
        QVendorEntity qVendorEntity = QVendorEntity.vendorEntity;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qPaymentEntity.paymentEventEntity.vendorEntity.vendorSeq.eq(vendorSeq));
        if (paymentEventStatus != null) {
            builder.and(
                    qPaymentEntity.paymentEventEntity.paymentEventStatus.eq(PAYMENT_EVENT_STATUS.valueOf(paymentEventStatus))
                            .and(qPaymentEntity.paymentStatus.eq(PAYMENT_STATUS.SUCCESS))
            );
        } else {
            builder.and(
                    qPaymentEntity.paymentEventEntity.paymentEventStatus.in(PAYMENT_EVENT_STATUS.CONFIRMED, PAYMENT_EVENT_STATUS.SETTLED)
                            .and(qPaymentEntity.paymentStatus.eq(PAYMENT_STATUS.SUCCESS))
            );
        }

        return queryFactory
                .select(Projections.constructor(
                        GetVendorSettlementManagementProgressResponse.class,
                        qPaymentEntity.paymentEventEntity.vendorEntity.vendorSeq,
                        qPaymentEntity.paymentEventEntity.paymentEventStatus,
                        qPaymentEntity.paymentEventEntity.solutionEntity.solutionName,
                        qPaymentEntity.amount,
                        qPaymentEntity.autoConfirmScheduledAt,
                        qLedgerEntity.settlementAmount,
                        qPaymentEntity.paymentEventEntity.consumerEntity.consumerSeq,
                        qPaymentEntity.paymentEventEntity.consumerEntity.consumerName
                ))
                .from(qPaymentEntity)
                .join(qPaymentEntity.paymentEventEntity, qPaymentEventEntity)
                .join(qPaymentEventEntity.vendorEntity, qVendorEntity)
                .join(qPaymentEventEntity.consumerEntity, qConsumerEntity)
                .join(qPaymentEventEntity.solutionEntity, qSolutionEntity)
                .leftJoin(qLedgerEntity).on(
                        qLedgerEntity.consumerEntity.eq(qConsumerEntity)
                                .and(qLedgerEntity.vendorEntity.eq(qVendorEntity))
                )
                .where(builder)
                .orderBy(qPaymentEntity.createdAt.desc())
                .offset(start)
                .limit(end - start)
                .fetch();
    }
}
