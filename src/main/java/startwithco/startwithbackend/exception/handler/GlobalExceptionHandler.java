package startwithco.startwithbackend.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import startwithco.startwithbackend.exception.*;
import startwithco.startwithbackend.log.service.ExceptionLogService;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    // [수정] ObjectMapper를 인스턴스 변수로 분리하여 재사용성 향상
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String EMPTY_BODY = "[본문 없음]";
    private static final String EXTRACTION_FAILED = "[본문 추출 실패]";
    private static final String UNKNOWN_METHOD = "UNKNOWN";

    private final ExceptionLogService exceptionLogService;

    @ExceptionHandler({ServerException.class, BadRequestException.class, NotFoundException.class, UnauthorizedException.class, ConflictException.class})
    public ResponseEntity<ErrorResponse> handleCustomException(final RuntimeException exception, final HttpServletRequest request) {

        if (exception instanceof CustomBaseException ex) {
            // [수정] 스택 트레이스 존재 여부 확인 로직 개선
            String methodName = ex.getStackTrace().length > 0
                    ? ex.getStackTrace()[0].toString()
                    : UNKNOWN_METHOD;

            String requestBody = getRequestBody(request);

            exceptionLogService.saveExceptionLogEntity(
                    ex.getHttpStatus(),
                    ex.getCode(),
                    ex.getMessage(),
                    request.getRequestURI(),
                    methodName,
                    requestBody
            );

            return ResponseEntity.status(ex.getHttpStatus())
                    .body(new ErrorResponse(ex.getHttpStatus(), ex.getMessage(), ex.getCode()));
        }

        // [수정] 매직 넘버를 상수로 분리
        final int INTERNAL_SERVER_ERROR_STATUS = 500;
        log.error("Unhandled exception caught: ", exception);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR_STATUS)
                .body(new ErrorResponse(INTERNAL_SERVER_ERROR_STATUS, "서버 내부 오류", "INTERNAL_SERVER_ERROR"));
    }

    private String getRequestBody(HttpServletRequest request) {
        try {
            String contentType = request.getContentType();

            if (contentType != null && contentType.contains(CONTENT_TYPE_JSON)) {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = request.getReader()) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                }

                // [수정] 변수명 개선 (raw -> requestBody)
                String requestBody = sb.toString();
                if (requestBody.isBlank()) return EMPTY_BODY;

                @SuppressWarnings("unchecked")
                Map<String, Object> json = OBJECT_MAPPER.readValue(requestBody, Map.class);
                Object requestData = json.get("request");

                if (requestData instanceof String str) {
                    try {
                        Object nested = OBJECT_MAPPER.readValue(str, Object.class);
                        return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(nested);
                    } catch (Exception e) {
                        return str;
                    }
                }

                if (requestData != null) {
                    return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(requestData);
                }

                return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            } else {
                Map<String, String[]> paramMap = request.getParameterMap();
                if (paramMap.isEmpty()) return EMPTY_BODY;

                Map<String, Object> resultMap = new LinkedHashMap<>();
                for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
                    String key = entry.getKey();
                    String[] values = entry.getValue();
                    resultMap.put(key, values.length == 1 ? values[0] : Arrays.asList(values));
                }

                return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(resultMap);
            }
        } catch (Exception e) {
            return EXTRACTION_FAILED;
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class ErrorResponse {
        private final int httpStatus;
        private final String message;
        private final String code;
    }
}
