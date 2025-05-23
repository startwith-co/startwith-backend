package startwithco.startwithbackend.solution.erp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import startwithco.startwithbackend.base.BaseResponse;
import startwithco.startwithbackend.exception.badRequest.BadRequestErrorResult;
import startwithco.startwithbackend.exception.badRequest.BadRequestException;
import startwithco.startwithbackend.exception.badRequest.BadRequestExceptionHandler;
import startwithco.startwithbackend.exception.conflict.ConflictExceptionHandler;
import startwithco.startwithbackend.exception.server.ServerExceptionHandler;
import startwithco.startwithbackend.solution.erp.service.ErpService;

import static org.apache.http.util.TextUtils.isBlank;
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
    @Operation(summary = "ERP 솔루션 생성 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "200 SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "S500", description = "500 INTERNAL SERVER EXCEPTION", content = @Content(schema = @Schema(implementation = ServerExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "B001", description = "400 BAD REQUEST EXCEPTION", content = @Content(schema = @Schema(implementation = BadRequestExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "IRCE002", description = "409 IDEMPOTENT REQUEST CONFLICT EXCEPTION", content = @Content(schema = @Schema(implementation = ConflictExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NFE002", description = "404 VENDOR NOT FOUND EXCEPTION", content = @Content(schema = @Schema(implementation = ConflictExceptionHandler.ErrorResponse.class))),
    })
    ResponseEntity<BaseResponse<SaveErpEntityResponse>> saveErpEntity(
            @Valid
            @RequestPart(value = "representImageUrl", required = false) MultipartFile representImageUrl,
            @RequestPart(value = "descriptionPdfUrl", required = false) MultipartFile descriptionPdfUrl,
            @RequestPart SaveErpEntityRequest request
    ) {
        if (request.vendorSeq() == null ||
                isBlank(request.solutionName()) ||
                isBlank(request.solutionDetail()) ||
                request.category() == null ||
                isBlank(request.industry()) ||
                isBlank(request.recommendedCompanySize()) ||
                isBlank(request.solutionImplementationType()) ||
                isBlank(request.specialist()) ||
                request.amount() == null ||
                request.sellType() == null ||
                request.duration() == null ||
                representImageUrl == null ||
                descriptionPdfUrl == null ||
                CollectionUtils.isEmpty(request.keyword())) {
            throw new BadRequestException(BadRequestErrorResult.BAD_REQUEST_EXCEPTION);
        }

        SaveErpEntityResponse response = erpService.saveErpEntity(request, representImageUrl, descriptionPdfUrl);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), response));
    }
}
