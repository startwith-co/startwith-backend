package startwithco.startwithbackend.solution.keyword.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.solution.keyword.domain.SolutionKeywordEntity;

@Repository
public interface SolutionKeywordEntityJpaRepository extends JpaRepository<SolutionKeywordEntity, Long> {
}
