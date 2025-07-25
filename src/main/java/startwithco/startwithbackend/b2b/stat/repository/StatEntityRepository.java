package startwithco.startwithbackend.b2b.stat.repository;

import startwithco.startwithbackend.b2b.stat.domain.StatEntity;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;

import java.util.List;

public interface StatEntityRepository {

    void deleteAllByVendor(VendorEntity vendor);

    List<StatEntity> findAllByVendor(VendorEntity vendor);

    void saveAll(List<StatEntity> statEntities);
}
