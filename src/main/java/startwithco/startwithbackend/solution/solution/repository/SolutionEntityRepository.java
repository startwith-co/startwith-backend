package startwithco.startwithbackend.solution.solution.repository;

import startwithco.startwithbackend.solution.solution.domain.SolutionEntity;
import startwithco.startwithbackend.solution.solution.util.CATEGORY;

import java.util.List;
import java.util.Optional;

public interface SolutionEntityRepository {
    SolutionEntity saveSolutionEntity(SolutionEntity solutionEntity);

    Optional<SolutionEntity> findByVendorSeqAndCategory(Long vendorSeq, CATEGORY category);

    List<SolutionEntity> findAllByVendorSeq(Long vendorSeq);

    Optional<SolutionEntity> findBySolutionSeq(Long solutionSeq);

    List<SolutionEntity> findBySpecialistAndCategoryAndIndustryAndBudget(String specialist, CATEGORY category, String industry, String budget, int start, int end);
}
