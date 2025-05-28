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

import java.io.IOException;

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
                    1. 광클 방지를 위한 disable 처리해주세요.\n
                    2. amount는 1보다 작을 수 없습니다.\n
                    3. 이전에 동일한 솔루션의 결제 승인이 완료되지 않았다면 결제 요청을 할 수 없습니다.\n
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_005", description = "존재하지 않는 솔루션입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_001", description = "존재하지 않는 벤더 기업입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_004", description = "존재하지 않는 수요 기업입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "CONFLICT_EXCEPTION_003", description = "이미 동일한 솔루션에 대한 결제 요청이 진행 중입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<SavePaymentEventEntityResponse>> savePaymentEventEntity(
            @Valid
            @RequestPart(value = "contractConfirmationUrl", required = false) MultipartFile contractConfirmationUrl,
            @RequestPart(value = "refundPolicyUrl", required = false) MultipartFile refundPolicyUrl,
            @RequestPart SavePaymentEventRequest request
    ) throws IOException {
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
                    1. 광클 방지를 위한 disable 처리해주세요.\n
                    2. category: BI, BPM, CMS, CRM, DMS, EAM, ECM, ERP, HR, HRM, KM, SCM, SI, SECURITY\n
                    3. paymentEventStatus: REQUESTED(결제 요청), CANCELLED(결제 요청 취소), CONFIRMED(구매 확정), SETTLED(정산 완료)\n
                    4. CONFIRMED(구매 확정), SETTLED(정산 완료)의 경우는 결제 승인이 완료된 결제 요청입니다.\n
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_002", description = "존재하지 않는 결제 요청입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_004", description = "구매 확정, 정산 완료 결제 요청이지만 결제 승인된 정보가 없습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<GetPaymentEventEntityResponse>> getPaymentEventEntity(
            @Valid
            @RequestParam(name = "paymentEventSeq") Long paymentEventSeq
    ) {
        if (paymentEventSeq == null) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "요청 데이터 오류입니다.",
                    getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
            );
        }

        GetPaymentEventEntityResponse response = paymentEventService.getPaymentEventEntity(paymentEventSeq);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), response));
    }

    @DeleteMapping(
            name = "결제 요청 취소"
    )
    @Operation(
            summary = "결제 요청 취소 API",
            description = """
                    1. 광클 방지를 위한 disable 처리해주세요.\n
                    2. 해당 결제 요청이 이미 결제 승인 완료면 결제 요청 취소가 불가능합니다.\n
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_002", description = "존재하지 않는 결제 요청입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_005", description = "이미 결제 승인된 결제 요청입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
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
                    1. 이미 결제 승인된 EVENT는 조회할 수 없습니다.\n
                    2. category: BI, BPM, CMS, CRM, DMS, EAM, ECM, ERP, HR, HRM, KM, SCM, SI, SECURITY\n
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_002", description = "존재하지 않는 결제 요청입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_005", description = "이미 결제 승인된 결제 요청입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<GetPaymentEventEntityOrderResponse>> getPaymentEventEntityOrder(
            @Valid
            @RequestParam(name = "paymentEventSeq") Long paymentEventSeq
    ) {
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
