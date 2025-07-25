package startwithco.startwithbackend.b2b.chat.repository;

import startwithco.startwithbackend.b2b.chat.domain.ChatEntity;

import java.util.Optional;

public interface ChatEntityRepository {
    ChatEntity saveChatEntity(ChatEntity chatEntity);

    Optional<ChatEntity> findByChatUniqueType(String chatUniqueType);
}
