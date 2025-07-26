package startwithco.startwithbackend.b2b.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.b2b.client.domain.ClientEntity;
import startwithco.startwithbackend.b2b.stat.domain.StatEntity;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;

import java.util.List;

@Repository
public interface ClientEntityJpaRepository extends JpaRepository<ClientEntity, Long> {

    void deleteAllByVendorEntity(VendorEntity vendor);

    List<ClientEntity> findAllByVendorEntity(VendorEntity vendor);
}
