package startwithco.startwithbackend.payment.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import startwithco.startwithbackend.base.BaseResponse;
import startwithco.startwithbackend.exception.BadRequestException;
import startwithco.startwithbackend.exception.code.ExceptionCodeMapper;
import startwithco.startwithbackend.exception.handler.GlobalExceptionHandler;
import startwithco.startwithbackend.payment.payment.service.PaymentService;

import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.getCode;
import static startwithco.startwithbackend.payment.payment.controller.request.PaymentRequest.*;
import static startwithco.startwithbackend.payment.payment.controller.response.PaymentResponse.*;

@RestController
@RequestMapping("/api/payment-service/payment")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "토스페이먼츠 PG사", description = "담당자(박종훈)")
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping(
            name = "결제 승인된 결제 상태 확인"
    )
    @Operation(
            summary = "결제 승인된 결제 상태 확인 API",
            description = """
                    1. 결제 승인된 결제의 상태를 확인할 수 있는 API 입니다.
                    2. Param의 orderId의 경우 결제 승인 API 결제 승인 성공 시 UNIQUE 하게 반환되는 값입니다.
                    3. paymentEventStatus: REQUESTED(결제 요청), CANCELLED(결제 요청 취소), CONFIRMED(구매 확정), SETTLED(정산 완료)
                    4. method: CARD(카드 결제), VIRTUAL_ACCOUNT(가상 계좌)
                    5. paymentStatus: IN_PROGRESS(결제 승인 중), SUCCESS(결제 승인 성공), FAILURE(결제 승인 실패), CANCELLED(결제 요청 취소), REFUNDED(환불 처리)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", content = @Content(mediaType = "application/json", schema = @Schema(oneOf = {TossCardPaymentApprovalResponse.class, TossVirtualAccountPaymentResponse.class}))),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_002", description = "존재하지 않는 결제 요청입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<GetTossPaymentApprovalResponse>> getTossPaymentApproval(@RequestParam(value = "orderId", required = false) String orderId) {
        if (orderId == null || orderId.isEmpty()) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "요청 데이터 오류입니다.",
                    getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
            );
        }

        GetTossPaymentApprovalResponse response = paymentService.getTossPaymentApproval(orderId);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), response));
    }

    @PostMapping(
            name = "토스페이먼츠 PG사 연동 결제 승인 (가상계좌, 카드 결제)"
    )
    @Operation(
            summary = "토스페이먼츠 PG사 결제 승인 API (가상계좌, 카드 결제)",
            description = """
                    1. 광클 방지를 위한 disable 처리해주세요.
                    2. amount의 경우 부가세 포함한 가격을 보내야합니다.
                    3. 만약 결제 요청의 상태가 REQUEST가 아닐 경우 결제가 진행되지 않습니다.
                    4. paymentKey의 경우 SuccessURL에서 받은 값, orderId의 경우 결제 요청 조회에서 오는 orderId 값을 넘겨주시면 됩니다.
                    5. SERVER - TOSS 사이 간 orderId로 멱등성 처리가 돼 있습니다.
                    6. 만약 결제 승인 오류가 나게 되면 중복 결제 방지를 위해 해당 결제의 PaymentEvent에 **orderId가 새롭게 발급**됩니다.**
                    7. 결제 승인 과정에서 요청 시간 만료와 같은 상황으로 인해 결제 실패 처리가 될 수 있습니다.
                    8. 따라서 **결제 승인하기 전 반드시 결제 요청 조회 후 확인된 orderId로 결제 승인 해야합니다.**
                    7. 카드 결제, 가상 계좌 결제 승인 모두 이 API를 사용하지만 Response의 method("카드", "가상계좌")에 따라 반환값이 다릅니다.
                    8. 카드 결제 Response: TossCardPaymentApprovalResponse, 가상 계좌 Response: TossVirtualAccountPaymentResponse 입니다.
                    9. 가상계좌 개발자 센터: https://docs.tosspayments.com/guides/v2/payment-window/integration-virtual-account
                    10. 카드 결제 개발자 센터: https://docs.tosspayments.com/guides/payment/integration
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", content = @Content(mediaType = "application/json", schema = @Schema(oneOf = {TossCardPaymentApprovalResponse.class, TossVirtualAccountPaymentResponse.class}))),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_002", description = "존재하지 않는 결제 요청입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_002", description = "결제 요청이 REQUEST 상태가 아닙니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_003", description = "결제 금액이 TOSS PAYMENT 승인 금액과 다릅니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_004", description = "해당 결제 요청은 승인할 수 없습니다. 유효하지 않은 주문이거나 이미 처리된 결제입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_005", description = "중복된 결제 데이터가 존재합니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_003", description = "결제 응답 파싱 중 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_006", description = "토스페이먼츠 결제 승인 실패", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_007", description = "WebClient 응답 에러가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    public Mono<ResponseEntity<BaseResponse<?>>> tossPaymentApproval(@RequestBody TossPaymentApprovalRequest request) {
        request.validate();

        return paymentService.tossPaymentApproval(request)
                .map(response -> ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), response)));
    }

    @PostMapping(
            value = "/refund",
            name = "토스페이먼츠 PG사 환불 (가상계좌, 카드 결제)"
    )
    @Operation(
            summary = "토스페이먼츠 PG사 결제 환불 API (가상계좌, 카드 결제)",
            description = """
                    1. 광클 방지를 위한 disable 처리해주세요.
                    2. 플랫폼 환불 정책 상 부분 환불은 불가능 합니다.
                    3. 환불의 경우 24이내 3가지(카드 결제 취소, 가상 계좌 입금 전, 가상 계좌 입금 후)가 존재합니다.
                    4. 아래는 환불 가능한 결제 상태 입니다. **결제 승인된 결제 상태 확인 API를 통해 결제 승인된 데이터의 상태를 확인할 수 있습니다.**
                        - 카드 결제: METHOD(CARD), PAYMENT_STATUS(SUCCESS), PAYMENT_EVENT_STATUS(CONFIRMED)
                        - 가상 계좌 입금 후: METHOD(VIRTUAL_ACCOUNT), PAYMENT_STATUS(SUCCESS), PAYMENT_EVENT_STATUS(CONFIRMED)
                        - 가상 계좌 입금 전: METHOD(VIRTUAL_ACCOUNT), PAYMENT_STATUS(IN_PROGRESS), PAYMENT_EVENT_STATUS(REQUESTED)
                    5. 가상 계좌 입금 전의 경우 결제 승인이 이루어지지 않았기 때문에 가상 계좌 요청 24시간 이내를 기준으로 계산합니다.
                    6. 24시간이 지난 가상 계좌 입금의 경우 서버에서 자동으로 결제 취소 처리합니다.
                    7. 카드 결제 취소와 가상 계좌 입금 전의 경우 paymentEventSeq 값만 넘겨주시면 됩니다.
                    8. 가상 계좌 입금 후의 경우 bankCode(은행, 증권사 코드), accountNumber(환불 받을 계좌 번호), holderName(예금주)를 같이 넘겨야합니다.
                    9. 다음 은행 코드 중 하나를 사용해야 하며, 문자열 형태로 전달됩니다.
                       - 03: IBK기업은행
                       - 04: KB국민은행
                       - 07: Sh수협은행
                       - 11: NH농협은행
                       - 20: 우리은행
                       - 23: SC제일은행
                       - 27: 씨티은행
                       - 31: 대구은행 (iM뱅크)
                       - 32: 부산은행
                       - 34: 광주은행
                       - 35: 제주은행
                       - 37: 전북은행
                       - 39: 경남은행
                       - 45: 새마을금고
                       - 48: 신협
                       - 50: 저축은행중앙회
                       - 64: 산림조합
                       - 71: 우체국예금보험
                       - 81: 하나은행
                       - 88: 신한은행
                       - 89: 케이뱅크
                       - 90: 카카오뱅크
                       - 92: 토스뱅크
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", content = @Content(mediaType = "application/json", schema = @Schema(oneOf = {TossCardPaymentApprovalResponse.class, TossVirtualAccountPaymentResponse.class}))),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_009", description = "환불이 불가능한 결제입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_003", description = "존재하지 않는 결제입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<String>> refundTossPaymentApproval(@RequestBody RefundTossPaymentApprovalRequest request) {
        request.validate();

        paymentService.refundTossPaymentApprovalRequest(request);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), "success"));
    }

    @PostMapping(
            value = "/deposit-callback",
            name = "FE가 사용하는 API가 아닙니다."
    )
    @Operation(
            summary = "FE가 사용하는 API가 아닙니다. BE - TOSS 간 연동 API 입니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", content = @Content(mediaType = "application/json", schema = @Schema(oneOf = {TossCardPaymentApprovalResponse.class, TossVirtualAccountPaymentResponse.class}))),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_008", description = "무통장 입금 전 결제가 저장되지 않았습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<String>> tossPaymentDepositCallBack(@RequestBody TossPaymentDepositCallBackRequest request) {
        paymentService.tossPaymentDepositCallBack(request);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), "SUCCESS"));
    }
}
