package startwithco.startwithbackend.b2b.chat.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import startwithco.startwithbackend.b2b.chat.domain.ChatEntity;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ChatEntityRepositoryImpl implements ChatEntityRepository {
    private final ChatEntityJpaRepository repository;


    @Override
    public ChatEntity saveChatEntity(ChatEntity chatEntity) {
        return repository.save(chatEntity);
    }

    @Override
    public Optional<ChatEntity> findByChatUniqueType(String chatUniqueType) {
        return repository.findByChatUniqueType(chatUniqueType);
    }
}
