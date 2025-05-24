package startwithco.startwithbackend.b2b.consumer.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.b2b.consumer.domain.ConsumerEntity;

import java.util.Optional;

@Repository

public interface ConsumerJpaRepository extends JpaRepository<ConsumerEntity, Long> {

    Optional<ConsumerEntity> findByEmail(String email);

    @Query("SELECT c FROM ConsumerEntity c WHERE c.consumerSeq = :consumerSeq")
    Optional<ConsumerEntity> findByConsumerSeq(@Param("consumerSeq") Long consumerSeq);
}
