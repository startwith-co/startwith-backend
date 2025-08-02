package startwithco.startwithbackend.b2b.stat.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.b2b.client.repository.ClientEntityJpaRepository;
import startwithco.startwithbackend.b2b.stat.domain.StatEntity;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class StatEntityRepositoryImpl implements StatEntityRepository {

    private final StatEntityJpaRepository repository;


    @Override
    public void deleteAllByVendor(VendorEntity vendor) {
        repository.deleteAllByVendor(vendor);
    }

    @Override
    public List<StatEntity> findAllByVendor(VendorEntity vendor) {
        return repository.findAllByVendor(vendor);
    }

    @Override
    public void saveAll(List<StatEntity> statEntities) {
        repository.saveAll(statEntities);
    }
}
