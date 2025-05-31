package startwithco.startwithbackend.solution.effect.repository;

import startwithco.startwithbackend.solution.effect.domain.SolutionEffectEntity;

import java.util.List;

import static startwithco.startwithbackend.solution.solution.controller.response.SolutionResponse.GetSolutionEntityResponse.*;

public interface SolutionEffectEntityRepository {
    List<SolutionEffectEntity> saveAll(List<SolutionEffectEntity> solutionEffectEntities);

    void deleteAllBySolutionSeq(Long solutionSeq);

    List<SolutionEffectResponse> findAllBySolutionSeqCustom(Long solutionSeq);
}
