package startwithco.startwithbackend.b2b.vendor.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;
import startwithco.startwithbackend.exception.ServerException;
import startwithco.startwithbackend.exception.code.ExceptionCodeMapper;

import java.util.Optional;

import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.getCode;

@Repository
@RequiredArgsConstructor
public class VendorEntityRepositoryImpl implements VendorEntityRepository {
    private final VendorEntityJpaRepository repository;
    private final RedisTemplate<String, String> redisTemplate;

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
    public boolean existsByVendorSeq(Long vendorSeq) {
        return repository.existsByVendorSeq(vendorSeq);
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
