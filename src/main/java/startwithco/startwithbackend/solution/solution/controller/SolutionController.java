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
import startwithco.startwithbackend.solution.solution.controller.request.SolutionRequest;
import startwithco.startwithbackend.solution.solution.service.SolutionService;
import startwithco.startwithbackend.solution.solution.util.CATEGORY;

import java.util.List;

import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.getCode;
import static startwithco.startwithbackend.solution.solution.controller.request.SolutionRequest.*;
import static startwithco.startwithbackend.solution.solution.controller.request.SolutionRequest.SaveSolutionEntityRequest;
import static startwithco.startwithbackend.solution.solution.controller.response.SolutionResponse.*;
import static startwithco.startwithbackend.solution.solution.controller.response.SolutionResponse.SaveSolutionEntityResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/solution-service/solution")
@Tag(name = "솔루션", description = "담당자(박종훈)")
public class SolutionController {
    private final SolutionService solutionService;

    @PostMapping(
            name = "솔루션 생성",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "솔루션 생성 API",
            description = """
                    1. amount는 0보다 작을 수 없습니다.
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
    ) {
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
            summary = "솔루션 수정 API",
            description = """
                    1. amount는 0보다 작을 수 없습니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_005", description = "존재하지 않는 솔루션입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "CONFLICT_EXCEPTION_007", description = "이미 존재하는 카테고리입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "CONFLICT_EXCEPTION_002", description = "동시성 저장은 불가능합니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_002", description = "S3 UPLOAD 실패", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    ResponseEntity<BaseResponse<String>> modifySolutionEntity(
            @Valid
            @RequestPart(value = "representImageUrl", required = false) MultipartFile representImageUrl,
            @RequestPart(value = "descriptionPdfUrl", required = false) MultipartFile descriptionPdfUrl,
            @RequestPart ModifySolutionEntityRequest request
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

    @GetMapping(
            name = "솔루션 카테고리별 조회",
            value = "/category"
    )
    @Operation(summary = "솔루션 카테고리별 조회 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_001", description = "존재하지 않는 벤더 기업입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_008", description = "해당 기업이 작성한 카테고리 솔루션이 존재하지 않습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    ResponseEntity<BaseResponse<GetSolutionEntityResponse>> getSolutionEntityByCategory(
            @RequestParam(value = "vendorSeq", required = false) Long vendorSeq,
            @RequestParam(value = "category", required = false) String category
    ) {
        if (vendorSeq == null || category == null || category.isEmpty()) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "요청 데이터 오류입니다.",
                    getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
            );
        }

        try {
            CATEGORY.valueOf(category);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "요청 데이터 오류입니다.",
                    getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
            );
        }

        GetSolutionEntityResponse response = solutionService.getSolutionEntityByCategory(vendorSeq, CATEGORY.valueOf(category));

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), response));
    }

    @GetMapping(
            value = "/list",
            name = "전체 솔루션 조회 (필터링, 키워드 검색)"
    )
    @Operation(
            summary = "전체 솔루션 조회 (필터링, 키워드 검색) API",
            description = """
                    1. Param 값의 경우 전부 NULL 허용입니다.
                    2. NULL로 들어온 값은 필터링하지 않습니다.
                    3. budget의 경우 (전체, 500,000원 미만, 500,000~1,000,000원 미만, 1,000,000원~3,000,000원 미만, 3,000,000원~5,000,000원 미만, 5,000,000원~10,000,000원 미만, 10,000,000원 이상) 문자 그대로 보내주세요. Default의 경우 "전체" 보내주시면 됩니다.
                    4. start와 end는 시작과 끝의 인덱스 번호입니다. default: start = 0, end = 15
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    ResponseEntity<BaseResponse<List<GetAllSolutionEntityResponse>>> getAllSolutionEntity(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "industry", required = false) String industry,
            @RequestParam(value = "budget", required = false, defaultValue = "전체") String budget,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "start", defaultValue = "0") int start,
            @RequestParam(value = "end", defaultValue = "15") int end
    ) {
        if (category != null) {
            try {
                CATEGORY.valueOf(category);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
                );
            }
        }

        CATEGORY categoryEnum = (category != null) ? CATEGORY.valueOf(category) : null;
        List<GetAllSolutionEntityResponse> response = solutionService.getAllSolutionEntity(categoryEnum, industry, budget, keyword, start, end);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), response));
    }

    @GetMapping(name = "솔루션 조회")
    @Operation(summary = "솔루션 조회 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_005", description = "존재하지 않는 솔루션입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    ResponseEntity<BaseResponse<GetSolutionEntityResponse>> getSolutionEntity(@RequestParam(value = "solutionSeq", required = false) Long solutionSeq) {
        if (solutionSeq == null) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "요청 데이터 오류입니다.",
                    getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
            );
        }

        GetSolutionEntityResponse response = solutionService.getSolutionEntity(solutionSeq);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), response));
    }

    @DeleteMapping(name = "솔루션 삭제")
    @Operation(summary = "솔루션 삭제 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_001", description = "존재하지 않는 벤더 기업입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_008", description = "해당 기업이 작성한 카테고리 솔루션이 존재하지 않습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    ResponseEntity<BaseResponse<String>> deleteSolutionEntity(@RequestParam(value = "solutionSeq", required = false) Long solutionSeq) {
        if (solutionSeq == null) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "요청 데이터 오류입니다.",
                    getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
            );
        }

        solutionService.deleteSolutionEntity(solutionSeq);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), "SUCCESS"));
    }
}
