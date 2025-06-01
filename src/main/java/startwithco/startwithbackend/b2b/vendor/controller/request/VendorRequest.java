package startwithco.startwithbackend.b2b.vendor.controller.request;

import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import startwithco.startwithbackend.b2b.consumer.controller.request.ConsumerRequest;
import startwithco.startwithbackend.exception.BadRequestException;
import startwithco.startwithbackend.exception.code.ExceptionCodeMapper;

import java.time.LocalTime;
import java.util.Objects;

import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.getCode;

public class VendorRequest {

    public record SaveVendorRequest(
            String vendorName,
            MultipartFile businessLicenseImage,
            String managerName,
            String phoneNumber,
            String email,
            String password,
            String confirmPassword
    ) {

        public void validateSaveVendorRequest(VendorRequest.SaveVendorRequest request, MultipartFile businessLicenseImage) {

            if (request.vendorName() == null || request.email() == null
                    || request.password() == null || request.phoneNumber() == null || request.managerName() == null) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
                );
            }

            // 1. MIME 타입 검사
            String contentType = businessLicenseImage.getContentType();
            if (!Objects.equals(contentType, "application/pdf")) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
                );
            }

            // 2. 파일 확장자 검사 (추가 보안)
            String filename = businessLicenseImage.getOriginalFilename();
            if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
                );
            }

            // 3. 비밀번호 확인
            if(!request.password().equals(request.confirmPassword)) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
                );
            }

        }
    }

    public record SendMailRequest(
        String email
    ) {
        public void validateMailSendRequest(SendMailRequest request) {

            if (request.email == null) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.",ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
                );
            }
        }
    }

    public record VerifyCodeRequest(
            String code,
            String email
    ) {
        public void validateVerifyCodeRequest(VerifyCodeRequest request) {
            if (request.code == null || request.email == null) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.",ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
                );
            }
        }
    }

    public record LoginVendorRequest (
            String email,
            String password
    ) {
        public void validateLoginVendorRequest(VendorRequest.LoginVendorRequest request) {

            if (request.email == null || request.password == null) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
                );
            }
        }
    }

}