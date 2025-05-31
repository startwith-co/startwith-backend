package startwithco.startwithbackend.b2b.client.repository;

import startwithco.startwithbackend.b2b.client.domain.ClientEntity;

import java.util.List;

import static startwithco.startwithbackend.b2b.client.controller.response.ClientResponse.*;

public interface ClientEntityRepository {
    ClientEntity saveClientEntity(ClientEntity clientEntity);

    List<GetAllClientResponse> findAllByVendorSeqCustom(Long vendorSeq);
}
