package startwithco.startwithbackend.b2b.client.repository;

import java.util.List;

import static startwithco.startwithbackend.b2b.client.controller.response.ClientResponse.*;

public interface ClientEntityRepository {
    List<GetAllClientResponse> findAllByVendorSeqCustom(Long vendorSeq);
}
