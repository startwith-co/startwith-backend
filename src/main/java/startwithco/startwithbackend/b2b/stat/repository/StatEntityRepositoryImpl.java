package startwithco.startwithbackend.b2b.stat.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.b2b.stat.domain.QStatEntity;
import startwithco.startwithbackend.b2b.stat.domain.StatEntity;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;

import java.util.List;

import static startwithco.startwithbackend.b2b.vendor.controller.response.VendorResponse.*;

@Repository
@RequiredArgsConstructor
public class StatEntityRepositoryImpl implements StatEntityRepository {

    private final StatEntityJpaRepository repository;
    private final JPAQueryFactory queryFactory;


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

    @Override
    public List<StatResponse> findAllByVendorCustom(VendorEntity vendor) {
        QStatEntity qStatEntity = QStatEntity.statEntity;

        return queryFactory
                .select(Projections.constructor(
                        StatResponse.class,
                        qStatEntity.statType,
                        qStatEntity.percentage,
                        qStatEntity.label
                ))
                .from(qStatEntity)
                .where(qStatEntity.vendor.vendorSeq.eq(vendor.getVendorSeq()))
                .fetch();
    }
}
