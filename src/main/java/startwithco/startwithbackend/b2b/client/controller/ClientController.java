package startwithco.startwithbackend.b2b.client.controller;

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
import startwithco.startwithbackend.b2b.client.controller.request.ClientRequest;
import startwithco.startwithbackend.b2b.client.controller.response.ClientResponse;
import startwithco.startwithbackend.b2b.client.service.ClientService;
import startwithco.startwithbackend.base.BaseResponse;
import startwithco.startwithbackend.exception.BadRequestException;
import startwithco.startwithbackend.exception.code.ExceptionCodeMapper;
import startwithco.startwithbackend.exception.handler.GlobalExceptionHandler;
import startwithco.startwithbackend.solution.solution.controller.request.SolutionRequest;
import startwithco.startwithbackend.solution.solution.controller.response.SolutionResponse;

import java.io.IOException;
import java.util.List;

import static startwithco.startwithbackend.b2b.client.controller.request.ClientRequest.*;
import static startwithco.startwithbackend.b2b.client.controller.response.ClientResponse.*;
import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.getCode;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/b2b-service/client")
@Tag(name = "주요 기업 고객", description = "담당자(박종훈)")
public class ClientController {
    private final ClientService clientService;

    @PostMapping(
            name = "주요 기업 고객 생성",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "주요 기업 고객 생성 API"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_001", description = "존재하지 않는 벤더 기업입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    ResponseEntity<BaseResponse<SaveClientResponse>> saveClientEntity(
            @RequestPart(value = "logoImageUrl", required = false) MultipartFile logoImageUrl,
            @RequestPart SaveClientRequest request
    ) {
        request.validate();
        if (logoImageUrl == null || logoImageUrl.isEmpty()) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "요청 데이터 오류입니다.",
                    getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
            );
        }

        SaveClientResponse response = clientService.saveClientEntity(request, logoImageUrl);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), response));
    }

    @GetMapping(
            name = "주요 기업 고객 전체 조회"
    )
    @Operation(
            summary = "주요 기업 고객 전체 조회 API"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SUCCESS", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "SERVER_EXCEPTION_001", description = "내부 서버 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "BAD_REQUEST_EXCEPTION_001", description = "요청 데이터 오류입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_EXCEPTION_001", description = "존재하지 않는 벤더 기업입니다.", content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
    })
    ResponseEntity<BaseResponse<List<GetAllClientResponse>>> getAllClientEntity(
            @RequestParam(value = "vendorSeq", required = false) Long vendorSeq
    ) {
        if (vendorSeq == null) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "요청 데이터 오류입니다.",
                    getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
            );
        }

        List<GetAllClientResponse> response = clientService.getAllClientEntity(vendorSeq);

        return ResponseEntity.ok().body(BaseResponse.ofSuccess(HttpStatus.OK.value(), response));
    }
}
