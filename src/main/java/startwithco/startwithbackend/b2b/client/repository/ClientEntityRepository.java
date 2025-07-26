package startwithco.startwithbackend.b2b.client.repository;

import startwithco.startwithbackend.b2b.client.domain.ClientEntity;
import startwithco.startwithbackend.b2b.stat.domain.StatEntity;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;

import java.util.List;

import static startwithco.startwithbackend.b2b.client.controller.response.ClientResponse.*;

public interface ClientEntityRepository {
    ClientEntity saveClientEntity(ClientEntity clientEntity);

    List<GetAllClientResponse> findAllByVendorSeqCustom(Long vendorSeq);

    void deleteAllByVendor(VendorEntity vendor);

    void saveAll(List<ClientEntity> clientEntities);

}
