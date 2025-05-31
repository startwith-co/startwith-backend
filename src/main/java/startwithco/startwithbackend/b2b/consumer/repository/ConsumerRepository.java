package startwithco.startwithbackend.b2b.consumer.repository;

import startwithco.startwithbackend.b2b.consumer.domain.ConsumerEntity;

import java.util.Optional;

public interface ConsumerRepository {

    Optional<ConsumerEntity> findByEmail(String email);

    void save(ConsumerEntity consumerEntity);

    Optional<ConsumerEntity> findByConsumerSeq(Long solutionSeq);
}