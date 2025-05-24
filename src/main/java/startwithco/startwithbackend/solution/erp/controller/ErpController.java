package startwithco.startwithbackend.solution.erp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jdk.jfr.Description;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import startwithco.startwithbackend.base.BaseResponse;
import startwithco.startwithbackend.exception.badRequest.BadRequestExceptionHandler;
import startwithco.startwithbackend.exception.conflict.ConflictExceptionHandler;
import startwithco.startwithbackend.exception.notFound.NotFoundException;
import startwithco.startwithbackend.exception.notFound.NotFoundExceptionHandler;
import startwithco.startwithbackend.exception.server.ServerExceptionHandler;
import startwithco.startwithbackend.solution.erp.service.ErpService;

import static startwithco.startwithbackend.solution.erp.controller.request.ErpRequest.*;
import static startwithco.startwithbackend.solution.erp.controller.response.ErpResponse.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/solution-service/solution/erp")
public class ErpController {
    private final ErpService erpService;

    @PostMapping(
            name = "ERP 솔루션 생성",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "ERP 솔루션 생성 API 담당자(박종훈)",
            description = """
                          1. NULL 가능 데이터: solutionEffect\n
                          2. 중복 가능한 데이터의 경우 ','로 이어서 String으로 보내주세요\n
                          3. 중복 가능 데이터:\n
                          - industry(도입 가능 산업군)\n
                          - recommendedCompanySize(도입 가능 기업 규모)\n
                          - solutionImplementationType(솔루션 구축 형태)\n
                          - specialist(기능 특화)\n
                          
                          EX) recommendedCompanySize(도입 가능 기능 규모):\n
                          중소상공인,스타트업,소기업\n
                          """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "200 SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "S500", description = "500 INTERNAL SERVER EXCEPTION", content = @Content(schema = @Schema(implementation = ServerExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "B001", description = "400 BAD REQUEST EXCEPTION", content = @Content(schema = @Schema(implementation = BadRequestExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "IRCE002", description = "409 IDEMPOTENT REQUEST CONFLICT EXCEPTION", content = @Content(schema = @Schema(implementation = ConflictExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "VNFE002", description = "404 VENDOR NOT FOUND EXCEPTION", content = @Content(schema = @Schema(implementation = NotFoundExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "SCE003", description = "409 SOLUTION CONFLICT EXCEPTION", content = @Content(schema = @Schema(implementation = ConflictExceptionHandler.ErrorResponse.class))),
    })
    ResponseEntity<BaseResponse<SaveErpEntityResponse>> saveErpEntity(
            @Valid
            @RequestPart(value = "representImageUrl", required = true) MultipartFile representImageUrl,
            @RequestPart(value = "descriptionPdfUrl", required = true) MultipartFile descriptionPdfUrl,
            @RequestPart SaveErpEntityRequest request
    ) {
        request.validate(representImageUrl, descriptionPdfUrl);

        SaveErpEntityResponse response = erpService.saveErpEntity(request, representImageUrl, descriptionPdfUrl);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), response));
    }

    @PutMapping(
            name = "ERP 솔루션 수정",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "ERP 솔루션 수정 API 담당자(박종훈)",
            description = """
                          1. NULL 가능 데이터: solutionEffect\n
                          2. 중복 가능한 데이터의 경우 ','로 이어서 String으로 보내주세요\n
                          3. 중복 가능 데이터:\n
                          - industry(도입 가능 산업군)\n
                          - recommendedCompanySize(도입 가능 기업 규모)\n
                          - solutionImplementationType(솔루션 구축 형태)\n
                          - specialist(기능 특화)\n
                          
                          EX) recommendedCompanySize(도입 가능 기능 규모):\n
                          중소상공인,스타트업,소기업\n
                          """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "200 SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "S500", description = "500 INTERNAL SERVER EXCEPTION", content = @Content(schema = @Schema(implementation = ServerExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "B001", description = "400 BAD REQUEST EXCEPTION", content = @Content(schema = @Schema(implementation = BadRequestExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "IRCE002", description = "409 IDEMPOTENT REQUEST CONFLICT EXCEPTION", content = @Content(schema = @Schema(implementation = ConflictExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "VNFE002", description = "404 VENDOR NOT FOUND EXCEPTION", content = @Content(schema = @Schema(implementation = NotFoundExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "SNFE003", description = "404 SOLUTION NOT FOUND EXCEPTION", content = @Content(schema = @Schema(implementation = NotFoundExceptionHandler.ErrorResponse.class))),
    })
    ResponseEntity<BaseResponse<String>> modifyErpEntity(
            @Valid
            @RequestPart(value = "representImageUrl", required = true) MultipartFile representImageUrl,
            @RequestPart(value = "descriptionPdfUrl", required = true) MultipartFile descriptionPdfUrl,
            @RequestPart SaveErpEntityRequest request
    ) {
        request.validate(representImageUrl, descriptionPdfUrl);

        erpService.modifyErpEntity(request, representImageUrl, descriptionPdfUrl);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), "SUCCESS"));
    }
}
