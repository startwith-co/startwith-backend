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
import startwithco.startwithbackend.exception.badRequest.BadRequestErrorResult;
import startwithco.startwithbackend.exception.badRequest.BadRequestException;
import startwithco.startwithbackend.exception.badRequest.BadRequestExceptionHandler;
import startwithco.startwithbackend.exception.conflict.ConflictExceptionHandler;
import startwithco.startwithbackend.exception.notFound.NotFoundExceptionHandler;
import startwithco.startwithbackend.exception.server.ServerExceptionHandler;
import startwithco.startwithbackend.payment.paymentEvent.service.PaymentEventService;

import java.io.IOException;

import static startwithco.startwithbackend.payment.paymentEvent.controller.request.PaymentEventRequest.*;
import static startwithco.startwithbackend.payment.paymentEvent.controller.response.PaymentEventResponse.*;

@RestController
@RequestMapping("/api/payment-service/payment-event")
@RequiredArgsConstructor
@Tag(name = "결제", description = "담당자(박종훈)")
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
                    1. sellType: SINGLE(단건), SUBSCRIBE(월 구독형)\n
                    2. paymentEventRound: DOWN_PAYMENT(착수금), INTERIM_PAYMENT(중도금), BALANCE(잔금)
                    3. 광클 방지를 위한 disable 처리해주세요.\n
                    4. amount < 0 보다 작으면 안됩니다.\n
                    5. sellType = SINGLE 인데 paymentEventRound가 NULL이면 안됩니다.\n
                    6. sellType = SUBSCRIBE 인데 paymentEventRound에 값이 넘어오면 안됩니다.\n 
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "200 SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "S500", description = "500 INTERNAL SERVER EXCEPTION", content = @Content(schema = @Schema(implementation = ServerExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "B001", description = "400 BAD REQUEST EXCEPTION", content = @Content(schema = @Schema(implementation = BadRequestExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "SNFE003", description = "404 SOLUTION NOT FOUND EXCEPTION", content = @Content(schema = @Schema(implementation = NotFoundExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "VNFE002", description = "404 VENDOR NOT FOUND EXCEPTION", content = @Content(schema = @Schema(implementation = NotFoundExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "CNFE004", description = "404 CONSUMER NOT FOUND EXCEPTION", content = @Content(schema = @Schema(implementation = NotFoundExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "RPESCE007", description = "409 REQUESTED PAYMENT EVENT STATUS CONFLICT EXCEPTION", content = @Content(schema = @Schema(implementation = ConflictExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<SavePaymentEventEntityResponse>> savePaymentEventEntity(
            @Valid
            @RequestPart(value = "contractConfirmationUrl", required = true) MultipartFile contractConfirmationUrl,
            @RequestPart(value = "refundPolicyUrl", required = true) MultipartFile refundPolicyUrl,
            @RequestPart SavePaymentEventRequest request
    ) throws IOException {
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
                    1. 단일, 구독 상태에 따라 Response 되지 않을 수 있습니다. SUBSCRIBE의 경우 paymentEventRound 반환하지 않습니다.\n
                    2. sellType: SINGLE, SUBSCRIBE\n
                    3. paymentEventStatus: REQUESTED(결제 요청), CANCELLED(결제 요청 취소), CONFIRMED(구매 확정), SETTLED(정산 완료)\n
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "200 SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "S500", description = "500 INTERNAL SERVER EXCEPTION", content = @Content(schema = @Schema(implementation = ServerExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "B001", description = "400 BAD REQUEST EXCEPTION", content = @Content(schema = @Schema(implementation = BadRequestExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "PENFE005", description = "404 PAYMENT EVENT NOT FOUND EXCEPTION", content = @Content(schema = @Schema(implementation = NotFoundExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<GetPaymentEventEntityResponse>> getPaymentEventEntity(
            @Valid
            @RequestParam(name = "paymentEventSeq") Long paymentEventSeq
    ) {
        if (paymentEventSeq == null) {
            throw new BadRequestException(BadRequestErrorResult.BAD_REQUEST_EXCEPTION);
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
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "200 SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "S500", description = "500 INTERNAL SERVER EXCEPTION", content = @Content(schema = @Schema(implementation = ServerExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "B001", description = "400 BAD REQUEST EXCEPTION", content = @Content(schema = @Schema(implementation = BadRequestExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "PENFE005", description = "404 PAYMENT EVENT NOT FOUND EXCEPTION", content = @Content(schema = @Schema(implementation = NotFoundExceptionHandler.ErrorResponse.class))),
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
            summary = "결제하기 후 주문내역 API"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "200 SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "S500", description = "500 INTERNAL SERVER EXCEPTION", content = @Content(schema = @Schema(implementation = ServerExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "B001", description = "400 BAD REQUEST EXCEPTION", content = @Content(schema = @Schema(implementation = BadRequestExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "PENFE005", description = "404 PAYMENT EVENT NOT FOUND EXCEPTION", content = @Content(schema = @Schema(implementation = NotFoundExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<GetPaymentEventEntityOrderResponse>> getPaymentEventEntityOrder(
            @Valid
            @RequestParam(name = "paymentEventSeq") Long paymentEventSeq
    ) {
        if (paymentEventSeq == null) {
            throw new BadRequestException(BadRequestErrorResult.BAD_REQUEST_EXCEPTION);
        }

        GetPaymentEventEntityOrderResponse response = paymentEventService.getPaymentEventEntityOrder(paymentEventSeq);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), response));
    }
}
