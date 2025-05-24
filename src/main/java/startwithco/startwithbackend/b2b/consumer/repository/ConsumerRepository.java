package startwithco.startwithbackend.b2b.consumer.repository;

import startwithco.startwithbackend.b2b.consumer.domain.ConsumerEntity;

import java.util.Optional;

public interface ConsumerRepository {

    Optional<ConsumerEntity> isDuplicatedConsumerName(String consumerName);

    void save(ConsumerEntity consumerEntity);
}
