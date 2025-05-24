package startwithco.startwithbackend.b2b.consumer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.b2b.consumer.domain.ConsumerEntity;
import startwithco.startwithbackend.solution.erp.domain.ErpEntity;

import java.util.Optional;

@Repository

public interface ConsumerJpaRepository extends JpaRepository<ConsumerEntity, Long> {

    Optional<ConsumerEntity> findByConsumerName(String consumerName);
    Optional<ConsumerEntity> findByEmail(String email);

}
