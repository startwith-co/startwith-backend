package startwithco.startwithbackend.b2b.vendor.controller.request;

import org.springframework.web.multipart.MultipartFile;
import startwithco.startwithbackend.b2b.consumer.controller.request.ConsumerRequest;
import startwithco.startwithbackend.exception.badRequest.BadRequestErrorResult;
import startwithco.startwithbackend.exception.badRequest.BadRequestException;

import java.util.Objects;

public class VendorRequest {

    public record SaveVendorRequest(
            String vendorName,
            MultipartFile businessLicenseImage,
            String managerName,
            String phoneNumber,
            String email,
            String password
    ) {

        public void validateSaveVendorRequest(VendorRequest.SaveVendorRequest request, MultipartFile businessLicenseImage){

            if (request.vendorName() == null || request.email() == null
                    || request.password() == null || request.phoneNumber() == null || request.managerName() == null) {
                throw new BadRequestException(BadRequestErrorResult.BAD_REQUEST_EXCEPTION);
            }

            // 1. MIME 타입 검사
            String contentType = businessLicenseImage.getContentType();
            if (!Objects.equals(contentType, "application/pdf")) {
                throw new BadRequestException(BadRequestErrorResult.BAD_REQUEST_EXCEPTION);
            }

            // 2. 파일 확장자 검사 (추가 보안)
            String filename = businessLicenseImage.getOriginalFilename();
            if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
                throw new BadRequestException(BadRequestErrorResult.BAD_REQUEST_EXCEPTION);
            }

        }
    }
}