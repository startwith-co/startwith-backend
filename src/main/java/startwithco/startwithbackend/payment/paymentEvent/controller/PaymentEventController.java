package startwithco.startwithbackend.payment.paymentEvent.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import startwithco.startwithbackend.base.BaseResponse;
import startwithco.startwithbackend.exception.badRequest.BadRequestErrorResult;
import startwithco.startwithbackend.exception.badRequest.BadRequestException;
import startwithco.startwithbackend.exception.badRequest.BadRequestExceptionHandler;
import startwithco.startwithbackend.exception.conflict.ConflictExceptionHandler;
import startwithco.startwithbackend.exception.notFound.NotFoundExceptionHandler;
import startwithco.startwithbackend.exception.server.ServerExceptionHandler;
import startwithco.startwithbackend.payment.paymentEvent.service.PaymentEventService;

import static startwithco.startwithbackend.payment.paymentEvent.controller.request.PaymentEventRequest.*;
import static startwithco.startwithbackend.payment.paymentEvent.controller.response.PaymentEventResponse.*;

@RestController
@RequestMapping("/api/payment-service/payment")
@RequiredArgsConstructor
@Tag(name = "결제", description = "담당자(박종훈)")
public class PaymentEventController {
    private final PaymentEventService paymentEventService;

    @PostMapping(
            name = "결제 요청하기 생성"
    )
    @Operation(
            summary = "결제 요청하기 생성 API",
            description = """
                    1. SELL_TYPE: SINGLE, SUBSCRIBE\n
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "200 SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "S500", description = "500 INTERNAL SERVER EXCEPTION", content = @Content(schema = @Schema(implementation = ServerExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "B001", description = "400 BAD REQUEST EXCEPTION", content = @Content(schema = @Schema(implementation = BadRequestExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "IRCE002", description = "409 IDEMPOTENT REQUEST CONFLICT EXCEPTION", content = @Content(schema = @Schema(implementation = ConflictExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "SNFE003", description = "404 SOLUTION NOT FOUND EXCEPTION", content = @Content(schema = @Schema(implementation = NotFoundExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "VNFE002", description = "404 VENDOR NOT FOUND EXCEPTION", content = @Content(schema = @Schema(implementation = NotFoundExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "CNFE004", description = "404 CONSUMER NOT FOUND EXCEPTION", content = @Content(schema = @Schema(implementation = NotFoundExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<SavePaymentEventEntityResponse>> savePaymentEventEntity(
            @RequestBody SavePaymentEventRequest request
    ) {
        request.validate();

        SavePaymentEventEntityResponse response = paymentEventService.savePaymentEventEntity(request);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), response));
    }

    @GetMapping(
            name = "결제 요청하기 조회"
    )
    @Operation(
            summary = "결제 요청하기 조회 API",
            description = """
                    1. 결제 요청, 결제 요청 취소, 개발 완료 -> 상태에 따라 Response 되지 않을 수 있습니다. (actualDuration, paymentCompletedAt, developmentCompletedAt, autoConfirmScheduledAt)\n
                    2. SELL_TYPE: SINGLE, SUBSCRIBE\n
                    3. CATEGORY: BI, BPM, CMS, CRM, DMS, EAM, ECM, ERP, HR, HRM, KM, SCM, SI, SECURITY\n
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "200 SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "S500", description = "500 INTERNAL SERVER EXCEPTION", content = @Content(schema = @Schema(implementation = ServerExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "B001", description = "400 BAD REQUEST EXCEPTION", content = @Content(schema = @Schema(implementation = BadRequestExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "PENFE005", description = "404 PAYMENT EVENT NOT FOUND EXCEPTION", content = @Content(schema = @Schema(implementation = NotFoundExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<GetPaymentEventEntityResponse>> getPaymentEventEntity(
            @RequestParam(name = "paymentEventSeq") Long paymentEventSeq
    ) {
        if (paymentEventSeq == null) {
            throw new BadRequestException(BadRequestErrorResult.BAD_REQUEST_EXCEPTION);
        }

        GetPaymentEventEntityResponse response = paymentEventService.getPaymentEventEntity(paymentEventSeq);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), response));
    }

    @PutMapping(
            name = "개발 완료 알림"
    )
    @Operation(
            summary = "개발 완료 알림 API"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "200 SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "S500", description = "500 INTERNAL SERVER EXCEPTION", content = @Content(schema = @Schema(implementation = ServerExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "B001", description = "400 BAD REQUEST EXCEPTION", content = @Content(schema = @Schema(implementation = BadRequestExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "PENFE005", description = "404 PAYMENT EVENT NOT FOUND EXCEPTION", content = @Content(schema = @Schema(implementation = NotFoundExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<String>> modifyPaymentEventStatus(
            @RequestBody ModifyDevelopmentCompletedAt request
    ) {
        request.validate();

        paymentEventService.modifyDevelopmentCompletedAt(request);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), "SUCCESS"));
    }
}
