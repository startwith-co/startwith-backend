package startwithco.startwithbackend.b2b.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.b2b.chat.domain.ChatEntity;

import java.util.Optional;

@Repository
public interface ChatEntityJpaRepository extends JpaRepository<ChatEntity, Long> {
    @Query("""
            SELECT c
            FROM ChatEntity c
            WHERE c.chatUniqueType = :chatUniqueType
            """)
    Optional<ChatEntity> findByChatUniqueType(String chatUniqueType);
}
