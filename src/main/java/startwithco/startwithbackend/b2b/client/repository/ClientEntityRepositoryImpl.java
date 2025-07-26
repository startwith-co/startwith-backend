package startwithco.startwithbackend.b2b.client.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.b2b.client.domain.ClientEntity;
import startwithco.startwithbackend.b2b.client.domain.QClientEntity;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;

import java.util.List;

import static startwithco.startwithbackend.b2b.client.controller.response.ClientResponse.*;

@Repository
@RequiredArgsConstructor
public class ClientEntityRepositoryImpl implements ClientEntityRepository {
    private final ClientEntityJpaRepository repository;
    private final JPAQueryFactory queryFactory;

    @Override
    public ClientEntity saveClientEntity(ClientEntity clientEntity) {
        return repository.save(clientEntity);
    }

    @Override
    public List<GetAllClientResponse> findAllByVendorSeqCustom(Long vendorSeq) {
        QClientEntity qClientEntity = QClientEntity.clientEntity;

        return queryFactory
                .select(Projections.constructor(
                        GetAllClientResponse.class,
                        qClientEntity.clientSeq,
                        qClientEntity.logoImageUrl
                ))
                .from(qClientEntity)
                .where(qClientEntity.vendorEntity.vendorSeq.eq(vendorSeq))
                .fetch();
    }

    @Override
    public void deleteAllByVendor(VendorEntity vendor) {
        repository.deleteAllByVendorEntity(vendor);
    }

    @Override
    public void saveAll(List<ClientEntity> clientEntities) {
        repository.saveAll(clientEntities);
    }
}
