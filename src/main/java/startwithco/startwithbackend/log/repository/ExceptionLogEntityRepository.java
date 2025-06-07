package startwithco.startwithbackend.log.repository;

import startwithco.startwithbackend.log.domain.ExceptionLogEntity;

import java.util.List;

public interface ExceptionLogEntityRepository {
    ExceptionLogEntity saveExceptionLogEntity(ExceptionLogEntity exceptionLogEntity);

    List<ExceptionLogEntity> findAll(int start, int end);
}
