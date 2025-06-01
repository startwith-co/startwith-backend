package startwithco.startwithbackend.b2b.vendor.controller;

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
import startwithco.startwithbackend.b2b.consumer.controller.request.ConsumerRequest;
import startwithco.startwithbackend.b2b.consumer.controller.response.ConsumerResponse;
import startwithco.startwithbackend.b2b.vendor.controller.request.VendorRequest;
import startwithco.startwithbackend.b2b.vendor.controller.response.VendorResponse;
import startwithco.startwithbackend.b2b.vendor.service.VendorService;
import startwithco.startwithbackend.base.BaseResponse;
import startwithco.startwithbackend.exception.BadRequestException;
import startwithco.startwithbackend.exception.handler.GlobalExceptionHandler;
import startwithco.startwithbackend.payment.paymentEvent.util.PAYMENT_EVENT_STATUS;
import startwithco.startwithbackend.common.service.CommonService;

import java.util.List;

import static startwithco.startwithbackend.b2b.vendor.controller.request.VendorRequest.*;
import static startwithco.startwithbackend.b2b.vendor.controller.response.VendorResponse.*;
import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.*;
import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.getCode;

@RestController
@RequestMapping("/api/b2b-service/vendor")
@RequiredArgsConstructor
@Tag(name = "벤더 기업", description = "담당자(송인준)")
public class VendorController {
    private final VendorService vendorService;
    private final CommonService commonService;

    @GetMapping(
            value = "/category",
            name = "벤더 기업 생성 솔루션 카테고리 API"
    )
    @Operation(
            summary = "벤더 기업 생성 솔루션 카테고리 / 담당자(박종훈)",
            description = "1. CATEGORY: BI, BPM, CMS, CRM, DMS, EAM, ECM, ERP, HR, HRM, KM, SCM, SI, SECURITY"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_001", description = "존재하지 않는 벤더 기업입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<List<GetVendorSolutionCategoryResponse>>> getVendorSolutionCategory(@RequestParam(name = "vendorSeq") Long vendorSeq) {
        if (vendorSeq == null) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "요청 데이터 오류입니다.",
                    getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
            );
        }

        List<GetVendorSolutionCategoryResponse> response = vendorService.getVendorSolutionCategory(vendorSeq);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), response));
    }

    @PostMapping(
            name = "벤더 기업 가입",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "join Vendor API",
            description = "벤더 기업 가입 API"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "CONFLICT_EXCEPTION_001", description = "중복된 이메일입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "CONFLICT_EXCEPTION_002", description = "동시성 저장은 불가능합니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_002", description = "S3 UPLOAD 실패", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<String>> saveVendorEntity(
            @Valid
            @RequestPart SaveVendorRequest request,
            @RequestPart(name = "businessLicenseImage", required = true) MultipartFile businessLicenseImage
    ) {
        request.validateSaveVendorRequest(request, businessLicenseImage);

        vendorService.saveVendor(request, businessLicenseImage);


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
        vendorService.validateEmail(request.email());

        // 메일 전송
        String authKey = commonService.sendAuthKey(request.email());

        // 인증코드 저장
        commonService.saveAuthKey(request.email(), authKey, "vendor");

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
    public ResponseEntity<BaseResponse<String>> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {

        // DTO 유효성 검사
        request.validateVerifyCodeRequest(request);

        // 가입 여부 확인
        vendorService.validateEmail(request.email());

        // 코드 검증
        commonService.verifyCode(request,"vendor");

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), "SUCCESS"));
    }

    @GetMapping(
            value = "/dashboard/status",
            name = "벤더 기업 정산 운영 현황"
    )
    @Operation(
            summary = "벤더 기업 정산 운영 현황 API / 담당자(박종훈)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_001", description = "존재하지 않는 벤더 기업입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<GetVendorSettlementManagementStatusResponse>> getVendorSettlementManagementStatus(
            @RequestParam(value = "vendorSeq", required = false) Long vendorSeq
    ) {
        if (vendorSeq == null) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "요청 데이터 오류입니다.",
                    getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
            );
        }

        GetVendorSettlementManagementStatusResponse response = vendorService.getVendorSettlementManagementStatus(vendorSeq);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), response));
    }

    @GetMapping(
            value = "/dashboard/progress",
            name = "벤더 기업 정산 관리 현황"
    )
    @Operation(
            summary = "벤더 기업 정산 관리 현황 API / 담당자(박종훈)",
            description = """
                    1. start와 end는 시작과 끝의 인덱스 번호입니다. default: start = 0, end = 10
                    2. 데이터 개수의 경우 end - start개 반환합니다.
                    3. paymentEventStatus가 null일 경우 해당 벤더 기업의 전체 데이터를 반환합니다.
                    4. paymentEventStatus: REQUESTED(결제 대기), CONFIRMED(구매 확정), SETTLED(정산 완료)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_001", description = "존재하지 않는 벤더 기업입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<List<GetVendorSettlementManagementProgressResponse>>> getVendorSettlementManagementProgress(
            @RequestParam(value = "vendorSeq", required = false) Long vendorSeq,
            @RequestParam(value = "paymentEventStatus", required = false) String paymentEventStatus,
            @RequestParam(value = "start", defaultValue = "0") int start,
            @RequestParam(value = "end", defaultValue = "10") int end
    ) {
        if (vendorSeq == null) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "요청 데이터 오류입니다.",
                    getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
            );
        }
        if (paymentEventStatus != null) {
            try {
                PAYMENT_EVENT_STATUS.valueOf(paymentEventStatus);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
                );
            }

            if (!(PAYMENT_EVENT_STATUS.valueOf(paymentEventStatus) != PAYMENT_EVENT_STATUS.CONFIRMED ||
                    PAYMENT_EVENT_STATUS.valueOf(paymentEventStatus) != PAYMENT_EVENT_STATUS.SETTLED)) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
                );
            }
        }

        List<GetVendorSettlementManagementProgressResponse> response
                = vendorService.getVendorSettlementManagementProgress(vendorSeq, paymentEventStatus, start, end);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), response));
    }

    @PostMapping(value = "/login", name = "Vendor 로그인")
    @Operation(summary = "Vendor Login API", description = "Vendor Login API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_009", description = "존재하지 않는 이메일 입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_007", description = "비밀번호가 일치하지 않습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_010", description = "Redis 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<LoginVendorResponse>> loginConsumer(@Valid @RequestBody VendorRequest.LoginVendorRequest request) {

        // DTO 유효성 검사
        request.validateLoginVendorRequest(request);

//        ConsumerResponse.LoginConsumerResponse login = consumerService.login(request);
        LoginVendorResponse login = vendorService.login(request);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), login));
    }

    @GetMapping(
            name = "벤더 기업 조회"
    )
    @Operation(summary = "벤더 기업 조회 API", description = "벤더 기업 조회 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_001", description = "존재하지 않는 벤더 기업입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<GetVendorInfo>> getVendorInfo(@RequestParam(name = "vendorSeq") Long vendorSeq) {
        if (vendorSeq == null) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "요청 데이터 오류입니다.",
                    getCode("요청 데이터 오류입니다.", ExceptionType.BAD_REQUEST)
            );
        }

        GetVendorInfo response = vendorService.getVendorInfo(vendorSeq);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), response));
    }
}
