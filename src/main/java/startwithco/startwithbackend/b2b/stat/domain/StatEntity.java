package startwithco.startwithbackend.b2b.stat.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import startwithco.startwithbackend.common.util.STAT_TYPE;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;
import startwithco.startwithbackend.base.BaseTimeEntity;

@Entity
@Table(name = "STAT_ENTITY")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@SuperBuilder
public class StatEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stat_seq")
    private Long statSeq;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "percentage", nullable = false)
    private Long percentage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_seq")
    private VendorEntity vendor;

    @Enumerated(EnumType.STRING)
    @Column(name = "stat_type", nullable = false)
    private STAT_TYPE statType;
}
