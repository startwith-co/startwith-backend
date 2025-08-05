package startwithco.startwithbackend.b2b.stat.controller;

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
import startwithco.startwithbackend.b2b.stat.service.StatService;
import startwithco.startwithbackend.base.BaseResponse;
import startwithco.startwithbackend.exception.BadRequestException;
import startwithco.startwithbackend.exception.code.ExceptionCodeMapper;
import startwithco.startwithbackend.exception.handler.GlobalExceptionHandler;

import static startwithco.startwithbackend.b2b.stat.controller.response.StatResponse.*;
import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.getCode;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/b2b-service/stat")
@Tag(name = "통계 자료", description = "담당자(박종훈)")
public class StatController {
    private final StatService statService;

    @GetMapping(name = "통계 자료 조회")
    @Operation(summary = "통계 자료 조회 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_004", description = "존재하지 않는 수요 기업입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<GetVendorStatResponse>> getStatEntity(@RequestParam(name = "vendorSeq", required = false) Long vendorSeq) {
        if (vendorSeq == null) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "요청 데이터 오류입니다.",
                    getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
            );
        }

        GetVendorStatResponse response = statService.getStatEntity(vendorSeq);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), response));
    }
}
