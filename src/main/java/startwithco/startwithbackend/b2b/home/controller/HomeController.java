package startwithco.startwithbackend.b2b.home.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import startwithco.startwithbackend.base.BaseResponse;
import startwithco.startwithbackend.exception.BadRequestException;
import startwithco.startwithbackend.exception.handler.GlobalExceptionHandler;
import startwithco.startwithbackend.solution.solution.service.SolutionService;

import static startwithco.startwithbackend.b2b.home.controller.response.HomeResponse.*;
import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.*;
import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.getCode;

@RestController
@RequestMapping("/api/b2b-service/home")
@Tag(name = "홈 화면", description = "담당자(박종훈)")
@RequiredArgsConstructor
public class HomeController {
    private final SolutionService solutionService;

    @GetMapping(
            name = "도입 완료, 도입 예정된 솔루션"
    )
    @Operation(
            summary = "도입 완료, 도입 예정된 솔루션",
            description = """
                    1. category: BI, BPM, CMS, CRM, DMS, EAM, ECM, ERP, HR, HRM, KM, SCM, SI, SECURITY
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_012", description = "존재하지 않는 사용자입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<HomeCategoryResponse>> getCategory(@RequestParam(value = "seq", required = false) Long seq) {
        if (seq == null) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "요청 데이터 오류입니다.",
                    getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
            );
        }

        HomeCategoryResponse response = solutionService.getCategory(seq);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), response));
    }
}
