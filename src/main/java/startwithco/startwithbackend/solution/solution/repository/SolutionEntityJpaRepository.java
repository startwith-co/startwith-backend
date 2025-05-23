package startwithco.startwithbackend.solution.solution.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.solution.solution.domain.SolutionEntity;

@Repository
public interface SolutionEntityJpaRepository extends JpaRepository<SolutionEntity, Long> {

}
