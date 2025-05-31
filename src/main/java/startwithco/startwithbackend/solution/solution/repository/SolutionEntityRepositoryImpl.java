package startwithco.startwithbackend.solution.solution.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.solution.solution.domain.SolutionEntity;
import startwithco.startwithbackend.solution.solution.util.CATEGORY;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SolutionEntityRepositoryImpl implements SolutionEntityRepository {
    private final SolutionEntityJpaRepository repository;

    public SolutionEntity saveSolutionEntity(SolutionEntity solutionEntity) {
        return repository.save(solutionEntity);
    }

    @Override
    public Optional<SolutionEntity> findByVendorSeqAndCategory(Long vendorSeq, CATEGORY category) {
        return repository.findByVendorSeqAndCategory(vendorSeq, category);
    }

    @Override
    public List<SolutionEntity> findAllByVendorSeq(Long vendorSeq) {
        return repository.findAllByVendorSeq(vendorSeq);
    }

    @Override
    public Optional<SolutionEntity> findBySolutionSeq(Long solutionSeq) {
        return repository.findBySolutionSeq(solutionSeq);
    }
}
