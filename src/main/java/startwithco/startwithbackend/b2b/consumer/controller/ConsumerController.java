package startwithco.startwithbackend.b2b.consumer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import startwithco.startwithbackend.b2b.consumer.controller.request.ConsumerRequest;
import startwithco.startwithbackend.b2b.consumer.service.ConsumerService;
import startwithco.startwithbackend.b2b.vendor.controller.request.VendorRequest;
import startwithco.startwithbackend.base.BaseResponse;
import startwithco.startwithbackend.exception.BadRequestException;
import startwithco.startwithbackend.exception.code.ExceptionCodeMapper;
import startwithco.startwithbackend.common.service.CommonService;
import startwithco.startwithbackend.exception.handler.GlobalExceptionHandler;
import startwithco.startwithbackend.payment.paymentEvent.controller.response.PaymentEventResponse;

import java.util.List;

import static startwithco.startwithbackend.b2b.consumer.controller.request.ConsumerRequest.*;
import static startwithco.startwithbackend.b2b.consumer.controller.response.ConsumerResponse.*;
import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.getCode;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/b2b-service/consumer")
@Tag(name = "수요 기업", description = "담당자(송인준)")
public class ConsumerController {


    private final ConsumerService consumerService;
    private final CommonService commonService;


    @PostMapping(name = "수요 기업 가입")
    @Operation(summary = "join Consumer API", description = "수요 기업 가입 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.",
                    useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "S500",
                    description = "500 SERVER_ERROR",
                    content = @Content(
                            schema = @Schema(
                                    implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "DB002",
                    description = "400 Invalid DTO Parameter errors",
                    content = @Content(
                            schema = @Schema(
                                    implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "CEDCE002",
                    description = "409 CONSUMER_EMAIL_DUPLICATION_CONFLICT_EXCEPTION",
                    content = @Content(
                            schema = @Schema(
                                    implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<BaseResponse<String>> saveConsumer(@Valid @RequestBody SaveConsumerRequest request) {

        // DTO 유효성 검사
        request.validateSaveConsumerRequest(request);

        // 저장
        consumerService.saveConsumer(request);


        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), "SUCCESS"));
    }

    @PostMapping(value = "/email/send", name = "메일 전송")
    @Operation(summary = "Mail Send API", description = "인증 메일 전송 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_010", description = "Redis 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "CONFLICT_EXCEPTION_005", description = "이미 가입한 이메일 입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_009", description = "이메일 전송 중 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),

    })
    public ResponseEntity<BaseResponse<String>> sendMail(@Valid @RequestBody VendorRequest.SendMailRequest request) {

        // DTO 유효성 검사
        request.validateMailSendRequest(request);

        // 가입 여부 확인
        consumerService.validateEmail(request.email());

        // 메일 전송
        String authKey = commonService.sendAuthKey(request.email());

        // 인증코드 저장
        commonService.saveAuthKey(request.email(), authKey, "consumer");

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), "SUCCESS"));
    }

    @PostMapping(value = "/email/verify", name = "인증코드 검증")
    @Operation(summary = "Code Verify API", description = "인증코드 검증 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "CONFLICT_EXCEPTION_005", description = "이미 가입한 이메일 입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_006", description = "인증코드가 일치하지 않습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_006", description = "존재하지 않는 코드입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<String>> verifyCode(@Valid @RequestBody VendorRequest.VerifyCodeRequest request) {

        // DTO 유효성 검사
        request.validateVerifyCodeRequest(request);

        // 가입 여부 확인
        consumerService.validateEmail(request.email());

        // 코드 검증
        commonService.verifyCode(request,"consumer");

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), "SUCCESS"));
    }

    @PostMapping(value = "/login", name = "Consumer 로그인")
    @Operation(summary = "Consumer Login API", description = "Consumer Login API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_009", description = "존재하지 않는 이메일 입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_007", description = "비밀번호가 일치하지 않습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_010", description = "Redis 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<LoginConsumerResponse>> loginConsumer(@Valid @RequestBody ConsumerRequest.LoginConsumerRequest request) {

        // DTO 유효성 검사
        request.validateLoginConsumerRequest(request);

        LoginConsumerResponse login = consumerService.login(request);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), login));
    }


    @GetMapping(
            name = "수요 기업 조회"
    )
    @Operation(summary = "수요 기업 조회 API", description = "수요 기업 조회 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_004", description = "존재하지 않는 수요 기업입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<GetConsumerInfo>> getConsumerInfo(@RequestParam(name = "consumerSeq") Long consumerSeq) {
        if (consumerSeq == null) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "요청 데이터 오류입니다.",
                    getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
            );
        }

        GetConsumerInfo response = consumerService.getConsumerInfo(consumerSeq);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), response));
    }

    @PutMapping( name = "Consumer 업데이트")
    @Operation(summary = "Consumer Update API", description = "Consumer Update API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_004", description = "존재하지 않는 수요 기업입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<String>> updateConsumer(@Valid
                                                             @RequestPart ConsumerRequest.UpdateConsumerInfoRequest request,
                                                             @RequestPart("consumerImageUrl") MultipartFile consumerImageUrl) {

        // DTO 유효성 검사
        request.validateUpdateConsumerRequest(request);

        consumerService.updateConsumer(request, consumerImageUrl);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), "success"));
    }

}