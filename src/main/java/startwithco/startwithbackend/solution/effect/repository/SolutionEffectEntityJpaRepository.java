package startwithco.startwithbackend.solution.effect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.solution.effect.domain.SolutionEffectEntity;

@Repository
public interface SolutionEffectEntityJpaRepository extends JpaRepository<SolutionEffectEntity, Long> {
}
