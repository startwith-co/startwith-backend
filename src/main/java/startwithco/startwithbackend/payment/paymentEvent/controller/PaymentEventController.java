package startwithco.startwithbackend.payment.paymentEvent.controller;

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
import startwithco.startwithbackend.exception.handler.GlobalExceptionHandler;
import startwithco.startwithbackend.payment.paymentEvent.service.PaymentEventService;

import java.util.List;

import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.*;
import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.getCode;
import static startwithco.startwithbackend.payment.paymentEvent.controller.request.PaymentEventRequest.*;
import static startwithco.startwithbackend.payment.paymentEvent.controller.response.PaymentEventResponse.*;

@RestController
@RequestMapping("/api/payment-service/payment-event")
@RequiredArgsConstructor
@Tag(name = "결제 요청", description = "담당자(박종훈)")
public class PaymentEventController {
    private final PaymentEventService paymentEventService;

    @PostMapping(
            name = "결제 요청하기 생성",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "결제 요청하기 생성 API",
            description = """
                    1. 광클 방지를 위한 disable 처리해주세요.
                    2. amount는 1보다 작을 수 없습니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_005", description = "존재하지 않는 솔루션입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_001", description = "존재하지 않는 벤더 기업입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_004", description = "존재하지 않는 수요 기업입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "\"동시성 저장은 불가능합니다.\"", description = "동시성 저장은 불가능합니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<SavePaymentEventEntityResponse>> savePaymentEventEntity(
            @RequestPart(value = "contractConfirmationUrl", required = false) MultipartFile contractConfirmationUrl,
            @RequestPart(value = "refundPolicyUrl", required = false) MultipartFile refundPolicyUrl,
            @RequestPart SavePaymentEventRequest request
    ) {
        if (contractConfirmationUrl.isEmpty() || refundPolicyUrl.isEmpty()) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "요청 데이터 오류입니다.",
                    getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
            );
        }

        request.validate();

        SavePaymentEventEntityResponse response
                = paymentEventService.savePaymentEventEntity(request, contractConfirmationUrl, refundPolicyUrl);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), response));
    }

    @GetMapping(
            name = "결제 요청하기 조회"
    )
    @Operation(
            summary = "결제 요청하기 조회 API",
            description = """
                    1. 광클 방지를 위한 disable 처리해주세요.
                    2. category: BI, BPM, CMS, CRM, DMS, EAM, ECM, ERP, HR, HRM, KM, SCM, SI, SECURITY
                    3. 결제 승인되지 않았을 때의 Response
                        - GetCONFIRMEDPaymentEventEntityResponse
                    4. 결제 승인 됐을 때의 Response
                        - GetREQUESTEDPaymentEventEntityResponse
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", content = @Content(mediaType = "application/json", schema = @Schema(oneOf = {GetREQUESTEDPaymentEventEntityResponse.class, GetCONFIRMEDPaymentEventEntityResponse.class}))),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_002", description = "존재하지 않는 결제 요청입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<List<Object>>> getPaymentEventEntity(
            @RequestParam(value = "consumerSeq", required = false) Long consumerSeq,
            @RequestParam(value = "vendorSeq", required = false) Long vendorSeq
    ) {
        if (consumerSeq == null || vendorSeq == null) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "요청 데이터 오류입니다.",
                    getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
            );
        }

        List<Object> response = paymentEventService.getPaymentEventEntity(consumerSeq, vendorSeq);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), response));
    }

    @DeleteMapping(
            name = "결제 요청 취소"
    )
    @Operation(
            summary = "결제 요청 취소 API",
            description = """
                    1. 광클 방지를 위한 disable 처리해주세요.
                    2. 해당 결제 요청이 이미 결제 승인 완료면 결제 요청 취소가 불가능합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_002", description = "존재하지 않는 결제 요청입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<String>> deletePaymentEventEntity(
            @Valid
            @RequestBody DeletePaymentEventRequest request
    ) {
        request.validate();

        paymentEventService.deletePaymentEventEntity(request);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), "SUCCESS"));
    }

    @GetMapping(
            value = "/order",
            name = "결제하기 후 주문내역"
    )
    @Operation(
            summary = "결제하기 후 주문내역 API",
            description = """
                    1. category: BI, BPM, CMS, CRM, DMS, EAM, ECM, ERP, HR, HRM, KM, SCM, SI, SECURITY
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_002", description = "존재하지 않는 결제 요청입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<GetPaymentEventEntityOrderResponse>> getPaymentEventEntityOrder(@RequestParam(name = "paymentEventSeq") Long paymentEventSeq) {
        if (paymentEventSeq == null) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "요청 데이터 오류입니다.",
                    getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
            );
        }

        GetPaymentEventEntityOrderResponse response = paymentEventService.getPaymentEventEntityOrder(paymentEventSeq);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), response));
    }
}
