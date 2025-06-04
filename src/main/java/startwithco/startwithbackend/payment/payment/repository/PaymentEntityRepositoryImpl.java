package startwithco.startwithbackend.payment.payment.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.b2b.vendor.domain.QVendorEntity;
import startwithco.startwithbackend.exception.BadRequestException;
import startwithco.startwithbackend.payment.payment.domain.PaymentEntity;
import startwithco.startwithbackend.payment.payment.domain.QPaymentEntity;
import startwithco.startwithbackend.payment.payment.util.PAYMENT_STATUS;
import startwithco.startwithbackend.payment.paymentEvent.domain.QPaymentEventEntity;
import startwithco.startwithbackend.solution.solution.domain.QSolutionEntity;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentEntityRepositoryImpl implements PaymentEntityRepository {
    private final PaymentEntityJpaRepository repository;
    private final JPAQueryFactory queryFactory;

    @Override
    public PaymentEntity savePaymentEntity(PaymentEntity paymentEntity) {
        return repository.save(paymentEntity);
    }

    @Override
    public boolean canApproveTossPayment(String orderId, Long paymentEventSeq) {
        return repository.canApproveTossPayment(orderId, paymentEventSeq);
    }

    @Override
    public Optional<PaymentEntity> findBySecret(String secret) {
        return repository.findBySecret(secret);
    }

    @Override
    public Optional<PaymentEntity> findByOrderId(String orderId) {
        return repository.findByOrderId(orderId);
    }

    @Override
    public List<PaymentEntity> findAllByConsumerSeqAndPaymentStatus(Long consumerSeq, String paymentStatus, int start, int end) {
        QPaymentEntity qPaymentEntity = QPaymentEntity.paymentEntity;
        QPaymentEventEntity qPaymentEventEntity = QPaymentEventEntity.paymentEventEntity;
        QSolutionEntity qSolutionEntity = QSolutionEntity.solutionEntity;
        QVendorEntity qVendorEntity = QVendorEntity.vendorEntity;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qPaymentEntity.paymentEventEntity.consumerEntity.consumerSeq.eq(consumerSeq));

        if (paymentStatus != null) {
            PAYMENT_STATUS status = PAYMENT_STATUS.valueOf(paymentStatus);
            builder.and(qPaymentEntity.paymentStatus.eq(status));
        } else {
            builder.and(
                    qPaymentEntity.paymentStatus.eq(PAYMENT_STATUS.DONE)
                            .or(qPaymentEntity.paymentStatus.eq(PAYMENT_STATUS.SETTLED))
            );
        }

        return queryFactory
                .selectFrom(qPaymentEntity)
                .join(qPaymentEntity.paymentEventEntity, qPaymentEventEntity).fetchJoin()
                .join(qPaymentEventEntity.solutionEntity, qSolutionEntity).fetchJoin()
                .join(qPaymentEventEntity.vendorEntity, qVendorEntity).fetchJoin()
                .where(builder)
                .orderBy(qPaymentEntity.paymentCompletedAt.desc())
                .offset(start)
                .limit(end - start)
                .fetch();
    }

    @Override
    public Long countDONEStatusByVendorSeq(Long vendorSeq) {
        return repository.countDONEStatusByVendorSeq(vendorSeq);
    }

    @Override
    public Long countSETTLEDStatusByVendorSeq(Long vendorSeq) {
        return repository.countSETTLEDStatusByVendorSeq(vendorSeq);
    }
}