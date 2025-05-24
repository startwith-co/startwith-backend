package startwithco.startwithbackend.payment.paymentEvent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.payment.paymentEvent.domain.PaymentEventEntity;

@Repository
public interface PaymentEventEntityJpaRepository extends JpaRepository<PaymentEventEntity, Long> {

}
