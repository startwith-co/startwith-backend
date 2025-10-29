package startwithco.startwithbackend.log.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.log.domain.ExceptionLogEntity;
import startwithco.startwithbackend.log.domain.QExceptionLogEntity;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ExceptionLogEntityRepositoryImpl implements ExceptionLogEntityRepository {
    private final ExceptionLogEntityJpaRepository repository;
    private final JPAQueryFactory queryFactory;

    @Override
    public ExceptionLogEntity saveExceptionLogEntity(ExceptionLogEntity exceptionLogEntity) {
        return repository.save(exceptionLogEntity);
    }

    @Override
    public List<ExceptionLogEntity> findAll(int start, int end) {
        QExceptionLogEntity qExceptionLogEntity = QExceptionLogEntity.exceptionLogEntity;

        int limit = Math.max(0, end - start);
        
        return queryFactory
                .selectFrom(qExceptionLogEntity)
                .orderBy(qExceptionLogEntity.createdAt.desc())
                .offset(Math.max(0, start))
                .limit(limit)
                .fetch();
    }
}
