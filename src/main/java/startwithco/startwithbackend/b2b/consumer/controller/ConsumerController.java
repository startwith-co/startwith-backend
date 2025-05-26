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
import startwithco.startwithbackend.b2b.consumer.controller.request.ConsumerRequest;
import startwithco.startwithbackend.b2b.consumer.controller.response.ConsumerResponse;
import startwithco.startwithbackend.b2b.consumer.service.ConsumerService;
import startwithco.startwithbackend.base.BaseResponse;
import startwithco.startwithbackend.exception.badRequest.BadRequestErrorResult;
import startwithco.startwithbackend.exception.badRequest.BadRequestException;
import startwithco.startwithbackend.exception.badRequest.BadRequestExceptionHandler;
import startwithco.startwithbackend.exception.conflict.ConflictExceptionHandler;
import startwithco.startwithbackend.exception.notFound.NotFoundExceptionHandler;
import startwithco.startwithbackend.exception.server.ServerExceptionHandler;
import startwithco.startwithbackend.payment.paymentEvent.controller.response.PaymentEventResponse;

import static startwithco.startwithbackend.b2b.consumer.controller.request.ConsumerRequest.*;
import static startwithco.startwithbackend.b2b.consumer.controller.response.ConsumerResponse.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/b2b-service/consumer")
@Tag(name = "수요 기업", description = "담당자(송인준)")
public class ConsumerController {


    private final ConsumerService consumerService;


    @PostMapping(name = "수요 기업 가입")
    @Operation(summary = "join Consumer API", description = "수요 기업 가입 API")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "요청에 성공하였습니다.",
                    useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = "S500",
                    description = "500 SERVER_ERROR",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ServerExceptionHandler.ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "DB002",
                    description = "400 Invalid DTO Parameter errors",
                    content = @Content(
                            schema = @Schema(
                                    implementation = BadRequestExceptionHandler.ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "CEDCE002",
                    description = "409 CONSUMER_EMAIL_DUPLICATION_CONFLICT_EXCEPTION",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ConflictExceptionHandler.ErrorResponse.class)))
    })
    public ResponseEntity<BaseResponse<String>> saveConsumer(@Valid @RequestBody SaveConsumerRequest request) {

        // DTO 유효성 검사
        request.validateSaveConsumerRequest(request);

        // 저장
        consumerService.saveConsumer(request);


        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), "SUCCESS"));
    }

    @GetMapping(
            name = "수요기업 상세보기 조회"
    )
    @Operation(
            summary = "수요기업 상세보기 (개발 진행 중, 개발 완료, 구매 확정)조회 API / 담당자(박종훈)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "200 SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "S500", description = "500 INTERNAL SERVER EXCEPTION", content = @Content(schema = @Schema(implementation = ServerExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "B001", description = "400 BAD REQUEST EXCEPTION", content = @Content(schema = @Schema(implementation = BadRequestExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "CNFE004", description = "404 CONSUMER NOT FOUND EXCEPTION", content = @Content(schema = @Schema(implementation = NotFoundExceptionHandler.ErrorResponse.class))),
    })
    public ResponseEntity<BaseResponse<ConsumerDetailResponse>> getConsumerDetail(@RequestParam(name = "consumerSeq") Long consumerSeq) {
        if (consumerSeq == null) {
            throw new BadRequestException(BadRequestErrorResult.BAD_REQUEST_EXCEPTION);
        }

        ConsumerDetailResponse response = consumerService.getConsumerDetails(consumerSeq);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), response));
    }
}
