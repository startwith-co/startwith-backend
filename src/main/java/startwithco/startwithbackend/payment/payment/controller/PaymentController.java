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
import startwithco.startwithbackend.exception.handler.GlobalExceptionHandler;
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
            summary = "토스페이먼츠 PG사 연동 결제하기 API",
            description = """
                    1. 광클 방지를 위한 disable 처리해주세요.\n
                    2. amount의 경우 부가세 포함한 가격을 보내야합니다.\n
                    3. 만약 결제 요청의 상태가 REQUEST가 아닐 경우 결제가 진행되지 않습니다.\n
                    4. paymentKey의 경우 SuccessURL에서 받은 값, orderId의 경우 UUID 생성해서 넘겨주시면 됩니다.\n
                    5. SERVER - TOSS 사이 간 orderId로 멱등성 처리가 돼 있습니다.\n
                    6. 만약 결제 승인 오류가 나게 되면 해당 결제의 PaymentEvent에 orderId가 새롭게 발급됩니다. 다시 결제하고자 한다면 결제 요청 조회 후 새로운 orderId로 결제 승인 해야합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_002", description = "존재하지 않는 결제 요청입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_002", description = "결제 요청이 REQUEST 상태가 아닙니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_003", description = "결제 금액이 TOSS PAYMENT 승인 금액과 다릅니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_004", description = "해당 결제 요청은 승인할 수 없습니다. 유효하지 않은 주문이거나 이미 처리된 결제입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_005", description = "중복된 결제 데이터가 존재합니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_003", description = "결제 응답 파싱 중 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
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
