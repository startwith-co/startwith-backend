package startwithco.startwithbackend.b2b.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.b2b.client.domain.ClientEntity;

@Repository
public interface ClientEntityJpaRepository extends JpaRepository<ClientEntity, Long> {
}
