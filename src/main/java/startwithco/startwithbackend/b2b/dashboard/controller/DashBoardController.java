package startwithco.startwithbackend.b2b.dashboard.controller;

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
import startwithco.startwithbackend.b2b.consumer.service.ConsumerService;
import startwithco.startwithbackend.b2b.vendor.service.VendorService;
import startwithco.startwithbackend.base.BaseResponse;
import startwithco.startwithbackend.exception.BadRequestException;
import startwithco.startwithbackend.exception.handler.GlobalExceptionHandler;
import startwithco.startwithbackend.payment.payment.util.PAYMENT_STATUS;

import java.util.List;

import static startwithco.startwithbackend.b2b.consumer.controller.response.ConsumerResponse.*;
import static startwithco.startwithbackend.b2b.vendor.controller.response.VendorResponse.*;
import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.*;
import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.getCode;

@RestController
@RequestMapping("/api/b2b-service/dashboard")
@RequiredArgsConstructor
@Tag(name = "대시 보드", description = "담당자(박종훈)")
public class DashBoardController {
    private final VendorService vendorService;
    private final ConsumerService consumerService;

    @GetMapping(
            value = "/vendor/status",
            name = "벤더 기업 대시보드 운영 현황"
    )
    @Operation(
            summary = "벤더 기업 대시보드 운영 현황 API / 담당자(박종훈)",
            description = """
                    1. CONFIRMED: 구매확정
                    2. DONE: 정산대기
                    3. SETTLED: 정산완료
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_001", description = "존재하지 않는 벤더 기업입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<GetVendorDashboardResponse>> getVendorDashboard(@RequestParam(name = "vendorSeq", required = false) Long vendorSeq) {
        if (vendorSeq == null) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "요청 데이터 오류입니다.",
                    getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
            );
        }

        GetVendorDashboardResponse response = vendorService.getVendorDashboard(vendorSeq);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), response));
    }

    @GetMapping(
            value = "/vendor",
            name = "벤더 기업 대시보드 운영 현황"
    )
    @Operation(
            summary = "벤더 기업 대시보드 운영 현황 API / 담당자(박종훈)",
            description = """
                    1. paymentStatus: DONE(정산 대기), SETTLED(정산 완료)
                    2. paymentStatus를 보내지 않으면 DONE, SETTLED 전체 데이터 반환됩니다.
                    7. 넘어가는 paymentStatus 값에 따라 Response가 다릅니다.
                        - DONE: GetVendorDashboardDONEListResponse
                        - SETTLED: GetVendorDashboardSETTELEDListResponse
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", content = @Content(mediaType = "application/json", schema = @Schema(oneOf = {GetVendorDashboardDONEListResponse.class, GetVendorDashboardSETTELEDListResponse.class}))),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_001", description = "존재하지 않는 벤더 기업입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<List<Object>>> getVendorDashboardList(
            @RequestParam(name = "vendorSeq", required = false) Long vendorSeq,
            @RequestParam(name = "paymentStatus", required = false) String paymentStatus,
            @RequestParam(value = "start", defaultValue = "0") int start,
            @RequestParam(value = "end", defaultValue = "15") int end
    ) {
        if (vendorSeq == null) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "요청 데이터 오류입니다.",
                    getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
            );
        }

        if (paymentStatus != null) {
            try {
                PAYMENT_STATUS.valueOf(paymentStatus);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
                );
            }
        }

        List<Object> response = vendorService.getVendorDashboardList(vendorSeq, paymentStatus, start, end);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), response));
    }

    @GetMapping(
            value = "/consumer",
            name = "수요 기업 대시보드 조회"
    )
    @Operation(
            summary = "수요 기업 대시보드 조회 API / 담당자(박종훈)",
            description = """
                    1. paymentStatus: DONE(구매 확정), SETTLED(정산 완료)
                    2. paymentStatus가 NULL일 경우 DONE, SETTLED 전부 반환합니다.
                    3. 결제 승인 일자 기준 내림차순 정렬합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_004", description = "존재하지 않는 수요 기업입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<List<GetConsumerDashboardResponse>>> getConsumerDashBoard(
            @RequestParam(value = "consumerSeq", required = false) Long consumerSeq,
            @RequestParam(value = "paymentStatus", required = false) String paymentStatus,
            @RequestParam(value = "start", defaultValue = "0") int start,
            @RequestParam(value = "end", defaultValue = "4") int end
    ) {
        if (consumerSeq == null) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "요청 데이터 오류입니다.",
                    getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
            );
        }

        if (paymentStatus != null) {
            try {
                PAYMENT_STATUS.valueOf(paymentStatus);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
                );
            }
        }

        List<GetConsumerDashboardResponse> response = consumerService.getConsumerDashboard(consumerSeq, paymentStatus, start, end);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), response));
    }
}
