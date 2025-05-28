package startwithco.startwithbackend.solution.solution.controller;

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
import startwithco.startwithbackend.base.BaseResponse;
import startwithco.startwithbackend.exception.BadRequestException;
import startwithco.startwithbackend.exception.code.ExceptionCodeMapper;
import startwithco.startwithbackend.exception.handler.GlobalExceptionHandler;
import startwithco.startwithbackend.solution.solution.service.SolutionService;

import java.io.IOException;

import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.getCode;
import static startwithco.startwithbackend.solution.solution.controller.request.SolutionRequest.SaveSolutionEntityRequest;
import static startwithco.startwithbackend.solution.solution.controller.response.SolutionResponse.SaveSolutionEntityResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/solution-service/solution")
@Tag(name = "솔루션", description = "담당자(송인준)")
public class SolutionController {
    private final SolutionService solutionService;

    @PostMapping(
            name = "솔루션 생성",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "솔루션 생성 / 담당자(박종훈)",
            description = """
                    1. 광클 방지를 위한 disable 처리해주세요.\n
                    2. 중복 가능한 데이터의 경우 ','로 이어서 String으로 보내주세요. EX) 소기업,중기업,중견기업\n
                    3. category: BI, BPM, CMS, CRM, DMS, EAM, ECM, ERP, HR, HRM, KM, SCM, SI, SECURITY\n
                    4. DIRECTION: INCREASE, DECREASE\n
                    5. amount는 1보다 작을 수 없습니다.\n
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_001", description = "존재하지 않는 벤더 기업입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "CONFLICT_EXCEPTION_004", description = "해당 벤더의 해당 카테고리 솔루션이 이미 존재합니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "CONFLICT_EXCEPTION_002", description = "동시성 저장은 불가능합니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_002", description = "S3 UPLOAD 실패", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    ResponseEntity<BaseResponse<SaveSolutionEntityResponse>> saveSolutionEntity(
            @Valid
            @RequestPart(value = "representImageUrl", required = false) MultipartFile representImageUrl,
            @RequestPart(value = "descriptionPdfUrl", required = false) MultipartFile descriptionPdfUrl,
            @RequestPart SaveSolutionEntityRequest request
    ) throws IOException {
        request.validate();
        if (representImageUrl.isEmpty() || descriptionPdfUrl.isEmpty()) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "요청 데이터 오류입니다.",
                    getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
            );
        }

        SaveSolutionEntityResponse response = solutionService.saveSolutionEntity(request, representImageUrl, descriptionPdfUrl);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), response));
    }

    @PutMapping(
            name = "솔루션 수정",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "솔루션 수정 / 담당자(박종훈)",
            description = """
                    1. 광클 방지를 위한 disable 처리해주세요.\n
                    2. 중복 가능한 데이터의 경우 ','로 이어서 String으로 보내주세요. EX) 소기업,중기업,중견기업\n
                    3. category: BI, BPM, CMS, CRM, DMS, EAM, ECM, ERP, HR, HRM, KM, SCM, SI, SECURITY\n
                    4. DIRECTION: INCREASE, DECREASE\n
                    5. amount는 1보다 작을 수 없습니다.\n
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_001", description = "존재하지 않는 벤더 기업입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_005", description = "존재하지 않는 솔루션입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "CONFLICT_EXCEPTION_002", description = "동시성 저장은 불가능합니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_002", description = "S3 UPLOAD 실패", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    ResponseEntity<BaseResponse<String>> modifySolutionEntity(
            @Valid
            @RequestPart(value = "representImageUrl", required = false) MultipartFile representImageUrl,
            @RequestPart(value = "descriptionPdfUrl", required = false) MultipartFile descriptionPdfUrl,
            @RequestPart SaveSolutionEntityRequest request
    ) {
        request.validate();
        if (representImageUrl.isEmpty() || descriptionPdfUrl.isEmpty()) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "요청 데이터 오류입니다.",
                    getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
            );
        }

        solutionService.modifySolutionEntity(request, representImageUrl, descriptionPdfUrl);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), "SUCCESS"));
    }
}
