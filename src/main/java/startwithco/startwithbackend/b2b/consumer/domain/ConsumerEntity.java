package startwithco.startwithbackend.b2b.consumer.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import startwithco.startwithbackend.base.BaseTimeEntity;

@Entity
@Table(name = "CONSUMER_ENTITY")
@AllArgsConstructor
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
@Getter
@SuperBuilder
public class ConsumerEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "consumer_seq")
    private Long consumerSeq;

    @Column(name = "consumer_name", nullable = false)
    private String consumerName;

    @Column(name = "encoded_password", nullable = false)
    private String encodedPassword;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "industry", nullable = false)
    private String industry;

    @Column(name = "consumer_image_url", nullable = true)
    private String consumerImageUrl;
}
