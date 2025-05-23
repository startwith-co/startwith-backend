package startwithco.startwithbackend.solution.erp.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.solution.erp.domain.ErpEntity;

@Repository
@RequiredArgsConstructor
public class ErpEntityRepositoryImpl implements ErpEntityRepository {
    private final ErpEntityJpaRepository repository;

    @Override
    public ErpEntity saveErpEntity(ErpEntity erpEntity) {
        return repository.save(erpEntity);
    }
}
