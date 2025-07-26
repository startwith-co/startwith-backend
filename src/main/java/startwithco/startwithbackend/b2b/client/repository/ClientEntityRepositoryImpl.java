package startwithco.startwithbackend.b2b.client.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.b2b.client.domain.QClientEntity;

import java.util.List;

import static startwithco.startwithbackend.b2b.client.controller.response.ClientResponse.*;

@Repository
@RequiredArgsConstructor
public class ClientEntityRepositoryImpl implements ClientEntityRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<GetAllClientResponse> findAllByVendorSeqCustom(Long vendorSeq) {
        QClientEntity qClientEntity = QClientEntity.clientEntity;

        return queryFactory
                .select(Projections.constructor(
                        GetAllClientResponse.class,
                        qClientEntity.clientSeq,
                        qClientEntity.clientName,
                        qClientEntity.logoImageUrl
                ))
                .from(qClientEntity)
                .where(qClientEntity.vendorEntity.vendorSeq.eq(vendorSeq))
                .fetch();
    }
}
