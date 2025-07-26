package startwithco.startwithbackend.b2b.client.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;
import startwithco.startwithbackend.base.BaseTimeEntity;

@Entity
@Table(name = "CLIENT_ENTITY")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@SuperBuilder
public class ClientEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_seq")
    private Long clientSeq;

    @Column(name = "logo_image_url", nullable = false)
    private String logoImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_seq", nullable = false)
    private VendorEntity vendorEntity;
}
