package startwithco.startwithbackend.admin.settlement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class VendorDto {
    LocalDateTime registerCompletedAt;
    private String vendorName;
    private String managerName;
    private String phoneNumber;
    private String email;
    private boolean audit;
}
