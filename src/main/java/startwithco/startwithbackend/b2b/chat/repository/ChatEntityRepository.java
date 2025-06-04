package startwithco.startwithbackend.b2b.chat.repository;

import startwithco.startwithbackend.b2b.chat.domain.ChatEntity;

import java.util.List;

public interface ChatEntityRepository {
    ChatEntity saveChatEntity(ChatEntity chatEntity);

    List<ChatEntity> findAllBySenderSeqAndReceiverSeq(Long senderSeq, Long receiverSeq);
}
