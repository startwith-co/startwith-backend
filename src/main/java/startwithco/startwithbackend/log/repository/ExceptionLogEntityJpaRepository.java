package startwithco.startwithbackend.log.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.log.domain.ExceptionLogEntity;

@Repository
public interface ExceptionLogEntityJpaRepository extends JpaRepository<ExceptionLogEntity, Long> {
}
