package startwithco.startwithbackend.b2b.consumer.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import startwithco.startwithbackend.base.BaseTimeEntity;

@Entity
@Table(
        name = "CONSUMER_ENTITY",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email")}
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@SuperBuilder
public class ConsumerEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "consumer_seq")
    private Long consumerSeq;

    @Column(name = "consumer_name", nullable = false)
    private String consumerName;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "encoded_password", nullable = false)
    private String encodedPassword;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "industry", nullable = false)
    private String industry;

    @Column(name = "consumer_image_url", nullable = true)
    private String consumerImageUrl;

    public void update(String consumerName, String phoneNumber, String email, String industry, String consumerImageUrl) {
        this.consumerName = consumerName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.industry = industry;
        this.consumerImageUrl = consumerImageUrl;
    }
}
