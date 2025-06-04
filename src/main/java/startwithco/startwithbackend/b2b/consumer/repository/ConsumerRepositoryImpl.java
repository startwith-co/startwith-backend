package startwithco.startwithbackend.b2b.consumer.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.b2b.consumer.domain.ConsumerEntity;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ConsumerRepositoryImpl implements ConsumerRepository {

    private final ConsumerJpaRepository consumerJpaRepository;

    @Override
    public Optional<ConsumerEntity> findByEmail(String email) {
        return consumerJpaRepository.findByEmail(email);
    }

    @Override
    public void save(ConsumerEntity consumerEntity) {
        consumerJpaRepository.save(consumerEntity);
    }

    @Override
    public Optional<ConsumerEntity> findByConsumerSeq(Long solutionSeq) {
        return consumerJpaRepository.findByConsumerSeq(solutionSeq);
    }

    @Override
    public boolean existsByConsumerSeq(Long consumerSeq) {
        return consumerJpaRepository.existsByConsumerSeq(consumerSeq);
    }
}