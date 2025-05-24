package startwithco.startwithbackend.b2b.vendor.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import startwithco.startwithbackend.b2b.vendor.service.VendorService;
import startwithco.startwithbackend.base.BaseResponse;
import startwithco.startwithbackend.exception.badRequest.BadRequestErrorResult;
import startwithco.startwithbackend.exception.badRequest.BadRequestException;
import startwithco.startwithbackend.exception.badRequest.BadRequestExceptionHandler;
import startwithco.startwithbackend.exception.conflict.ConflictExceptionHandler;
import startwithco.startwithbackend.exception.notFound.NotFoundExceptionHandler;
import startwithco.startwithbackend.exception.server.ServerExceptionHandler;

import java.util.List;

import static startwithco.startwithbackend.b2b.vendor.controller.request.VendorRequest.*;
import static startwithco.startwithbackend.b2b.vendor.controller.response.VendorResponse.*;

@RestController
@RequestMapping("/api/b2b-service/vendor")
@RequiredArgsConstructor
@Tag(name = "벤더 기업", description = "담당자(송인준)")
public class VendorController {
    private final VendorService vendorService;

    @GetMapping(
            value = "/category",
            name = "벤더 기업 생성 솔루션 카테고리 API"
    )
    @Operation(
            summary = "벤더 기업 생성 솔루션 카테고리 / 담당자(박종훈)",
            description = "1. CATEGORY: BI, BPM, CMS, CRM, DMS, EAM, ECM, ERP, HR, HRM, KM, SCM, SI, SECURITY"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "200 SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "S500", description = "500 INTERNAL SERVER EXCEPTION", content = @Content(schema = @Schema(implementation = ServerExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "B001", description = "400 BAD REQUEST EXCEPTION", content = @Content(schema = @Schema(implementation = BadRequestExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "VNFE002", description = "404 VENDOR NOT FOUND EXCEPTION", content = @Content(schema = @Schema(implementation = NotFoundExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<List<GetVendorSolutionCategory>>> getVendorSolutionCategory(@RequestParam(name = "vendorSeq") Long vendorSeq) {
        if (vendorSeq == null) {
            throw new BadRequestException(BadRequestErrorResult.BAD_REQUEST_EXCEPTION);
        }

        List<GetVendorSolutionCategory> response = vendorService.getVendorSolutionCategory(vendorSeq);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), response));
    }

    @PostMapping(
            name = "벤더 기업 가입",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "join Vendor API",
            description = "벤더 기업 가입 API"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "S500", description = "500 SERVER_ERROR", content = @Content(schema = @Schema(implementation = ServerExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "DB002", description = "400 Invalid DTO Parameter errors", content = @Content(schema = @Schema(implementation = BadRequestExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "VNDCE001", description = "409 VENDOR_NAME_DUPLICATION_CONFLICT_EXCEPTION", content = @Content(schema = @Schema(implementation = ConflictExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<BaseResponse<String>> saveConsumer(
            @Valid
            @RequestPart SaveVendorRequest request,
            @RequestPart(name = "businessLicenseImage", required = true) MultipartFile businessLicenseImage
    ) {
        request.validateSaveVendorRequest(request, businessLicenseImage);

        vendorService.saveVendor(request, businessLicenseImage);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), "SUCCESS"));
    }
}
