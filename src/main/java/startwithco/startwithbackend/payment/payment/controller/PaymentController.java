package startwithco.startwithbackend.payment.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import startwithco.startwithbackend.base.BaseResponse;
import startwithco.startwithbackend.exception.badRequest.BadRequestExceptionHandler;
import startwithco.startwithbackend.exception.conflict.ConflictExceptionHandler;
import startwithco.startwithbackend.exception.notFound.NotFoundExceptionHandler;
import startwithco.startwithbackend.exception.server.ServerExceptionHandler;
import startwithco.startwithbackend.payment.payment.service.PaymentService;

import static startwithco.startwithbackend.payment.payment.controller.request.PaymentRequest.*;
import static startwithco.startwithbackend.payment.payment.controller.response.PaymentResponse.*;

@RestController
@RequestMapping("/api/b2b-service/payment")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "토스페이먼츠 PG사", description = "담당자(박종훈)")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping(
            name = "토스페이먼츠 PG사 연동 결제하기"
    )
    @Operation(
            summary = "토스페이먼츠 PG사 연동 결제하기 API"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "200 SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "S500", description = "500 INTERNAL SERVER EXCEPTION", content = @Content(schema = @Schema(implementation = ServerExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "B001", description = "400 BAD REQUEST EXCEPTION", content = @Content(schema = @Schema(implementation = BadRequestExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "PENFE005", description = "404 PAYMENT EVENT NOT FOUND EXCEPTION", content = @Content(schema = @Schema(implementation = NotFoundExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "IPESCE007", description = "409 INVALID PAYMENT EVENT STATUS CONFLICT EXCEPTION", content = @Content(schema = @Schema(implementation = ConflictExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "AMBRE002", description = "400 AMOUNT MISMATCH BAD REQUEST EXCEPTION", content = @Content(schema = @Schema(implementation = BadRequestExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "IRCE002", description = "409 IDEMPOTENT REQUEST CONFLICT EXCEPTION", content = @Content(schema = @Schema(implementation = ConflictExceptionHandler.ErrorResponse.class))),
    })
    public Mono<ResponseEntity<BaseResponse<TossPaymentApprovalResponse>>> tossPaymentApproval(
            @Valid
            @RequestBody TossPaymentApprovalRequest request
    ) {
        request.validate();

        return paymentService.tossPaymentApproval(request)
                .map(response -> ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), response)));
    }
}
