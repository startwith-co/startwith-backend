package startwithco.startwithbackend.b2b.chat.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import startwithco.startwithbackend.base.BaseTimeEntity;

@Entity
@Table(
        name = "CHAT_ENTITY",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email")}
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@SuperBuilder
public class ChatEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_seq")
    private Long chatSeq;

    @Column(name = "sender_seq", nullable = false)
    private Long senderSeq;

    @Column(name = "receiver_seq", nullable = false)
    private Long receiverSeq;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;
}
