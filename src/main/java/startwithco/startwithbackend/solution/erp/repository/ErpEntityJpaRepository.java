package startwithco.startwithbackend.solution.erp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.solution.erp.domain.ErpEntity;

@Repository
public interface ErpEntityJpaRepository extends JpaRepository<ErpEntity, Long> {

}
