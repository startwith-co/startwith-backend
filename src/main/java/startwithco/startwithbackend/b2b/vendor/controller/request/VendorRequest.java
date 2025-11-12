package startwithco.startwithbackend.b2b.vendor.controller.request;

import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import startwithco.startwithbackend.b2b.stat.util.STAT_TYPE;
import startwithco.startwithbackend.exception.BadRequestException;

import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.*;
import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.getCode;

public class VendorRequest {

    public record SaveVendorRequest(
            String vendorName,
            String managerName,
            String phoneNumber,
            String email,
            String password,
            String confirmPassword
    ) {
        public void validate(MultipartFile businessLicenseImage) {
            if (vendorName == null || email == null || password == null
                    || phoneNumber == null || managerName == null || vendorName.length() > 15) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
                );
            }

            String contentType = businessLicenseImage.getContentType();
            if (!Objects.equals(contentType, "application/pdf")) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
                );
            }

            String filename = businessLicenseImage.getOriginalFilename();
            if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
                );
            }

            if (!password.equals(confirmPassword)) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
                );
            }
        }
    }

    public record SendMailRequest(String email) {
        public void validate() {
            if (email == null) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
                );
            }
        }
    }

    public record VerifyCodeRequest(String code, String email) {
        public void validate() {
            if (code == null || email == null) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
                );
            }
        }
    }

    public record LoginVendorRequest(String email, String password) {
        public void validate() {
            if (email == null || password == null) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
                );
            }
        }
    }

    public record UpdateVendorInfoRequest(
            Long vendorSeq,
            String vendorName,
            String managerName,
            String phoneNumber,
            String email,
            Boolean audit,
            String accountNumber,
            String bank,
            String vendorExplanation,
            Boolean weekdayAvailable,
            LocalTime weekdayStartTime,
            LocalTime weekdayEndTime,
            Boolean weekendAvailable,
            LocalTime weekendStartTime,
            LocalTime weekendEndTime,
            Boolean holidayAvailable,
            LocalTime holidayStartTime,
            LocalTime holidayEndTime,
            Long orderCount,
            Long clientCount,
            List<StatInfo> stats
    ) {
        public void validate() {
            if (vendorSeq == null || vendorName.length() > 15
                    || vendorExplanation.length() > 500 || stats.size() > 6) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
                );
            }

            boolean hasInvalidStat = stats.stream()
                    .anyMatch(stat -> stat.percentage > 999 || stat.label.length() > 15);
            if (hasInvalidStat) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
                );
            }
        }

        public record StatInfo(
                String label,
                Long percentage,
                STAT_TYPE statType
        ) {
        }
    }

    public record ResetLinkRequest(String email, String vendorName) {
        public void validate() {
            if (email == null) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
                );
            }
        }
    }

    public record ResetPasswordRequest(String newPassword, String confirmPassword) {
        public void validate() {
            if (newPassword == null || confirmPassword == null) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
                );
            }

            if (!confirmPassword.equals(newPassword)) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
                );
            }
        }
    }

}