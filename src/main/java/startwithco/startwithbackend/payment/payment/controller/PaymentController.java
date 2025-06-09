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
import startwithco.startwithbackend.exception.handler.GlobalExceptionHandler;
import startwithco.startwithbackend.payment.payment.service.PaymentService;

import static startwithco.startwithbackend.payment.payment.controller.request.PaymentRequest.*;
import static startwithco.startwithbackend.payment.payment.controller.response.PaymentResponse.*;

@RestController
@RequestMapping("/api/payment-service/payment")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "토스페이먼츠 PG사", description = "담당자(박종훈)")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping(
            name = "토스페이먼츠 PG사 연동 결제 승인 (가상계좌, 카드 결제)"
    )
    @Operation(
            summary = "토스페이먼츠 PG사 결제 승인 API (가상계좌, 카드 결제)",
            description = """
                    1. 광클 방지를 위한 disable 처리해주세요.
                    2. amount의 경우 부가세 포함한 가격을 보내야합니다.
                    3. paymentKey의 경우 SuccessURL 에서 받은 값, orderId의 경우 UUID 값을 생성해서 넘겨주셔야합니다.
                    4. SERVER - TOSS 사이 간 orderId로 멱등성 처리가 돼 있습니다. 때문에 이전 결제 요청에서 결제 실패가 발생했을 경우 새로운 orderId 값을 만들어주셔야합니다.
                    5. 결제 실패 상황의 경우 웹훅 이벤트 URL 참고해주세요. (카드 결제: PAYMENT_STATUS_CHANGED, 가상 계좌: DEPOSIT_CALLBACK)
                    6. 넘어가는 method 값에 따라 Response가 다릅니다.
                        - "카드": TossCardPaymentApprovalResponse
                        - "가상계좌": TossVirtualAccountPaymentResponse
                    7. 가상계좌 개발자 센터: https://docs.tosspayments.com/guides/v2/payment-window/integration-virtual-account
                    8. 카드 결제 개발자 센터: https://docs.tosspayments.com/guides/payment/integration
                    9. 웹훅 이벤트: https://docs.tosspayments.com/reference/using-api/webhook-events#payment_status_changed
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", content = @Content(mediaType = "application/json", schema = @Schema(oneOf = {TossCardPaymentApprovalResponse.class, TossVirtualAccountPaymentResponse.class}))),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_002", description = "존재하지 않는 결제 요청입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_003", description = "결제 금액이 TOSS PAYMENT 승인 금액과 다릅니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_008", description = "해당 결제 요청은 승인할 수 없습니다. 결제 승인 진행 중입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_009", description = "지원하지 않는 결제 수단입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_003", description = "결제 응답 파싱 중 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "CONFLICT_EXCEPTION_006", description = "이미 해당 결제 요청에 대한 결제 정보가 존재합니다. 새롭게 결제 요청을 진행해야합니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
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
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
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
            value = "/deposit-callback"
    ) public ResponseEntity<BaseResponse<String>> tossPaymentDepositCallBack(@RequestBody TossPaymentDepositCallBackRequest request) {
        paymentService.tossPaymentDepositCallBack(request);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), "SUCCESS"));
    }
}
