package startwithco.startwithbackend.b2b.chat.controller.response;

public class ChatResponse {
    public record GetChatEntityFile(
            Long senderSeq,
            Long receiverSeq,
            String fileUrl
    ) {

    }
}
