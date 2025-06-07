package startwithco.startwithbackend.b2b.consumer.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.b2b.consumer.domain.ConsumerEntity;
import startwithco.startwithbackend.exception.ServerException;
import startwithco.startwithbackend.exception.code.ExceptionCodeMapper;

import java.util.Optional;

import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.getCode;

@Repository
@RequiredArgsConstructor
public class ConsumerRepositoryImpl implements ConsumerRepository {

    private final ConsumerJpaRepository consumerJpaRepository;
    private final RedisTemplate<String, String> redisTemplate;
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

    @Override
    public void saveBlackToken(String token) {
        try {
            redisTemplate.opsForValue().set(
                    token,
                    "Black Token"
            );
        } catch (Exception e) {
            throw new ServerException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Redis 서버 오류가 발생했습니다.",
                    getCode("Redis 서버 오류가 발생했습니다.", ExceptionCodeMapper.ExceptionType.SERVER)
            );
        }
    }
}