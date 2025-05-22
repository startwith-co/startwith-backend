package startwithco.startwithbackend.exception.unauthorized;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class UnauthorizedExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler({UnauthorizedException.class})
    public ResponseEntity<ErrorResponse> handleException(final UnauthorizedException exception) {
        log.error("⚠️ Server Exception occurred: ", exception);

        return this.makeErrorResponseEntity(UnauthorizedErrorResult.UNAUTHORIZED_EXCEPTION);
    }

    private ResponseEntity<ErrorResponse> makeErrorResponseEntity(final UnauthorizedErrorResult errorResult) {
        return ResponseEntity.status(errorResult.getHttpStatus())
                .body(new ErrorResponse(errorResult.getHttpStatus(), errorResult.getMessage(), errorResult.getCode()));
    }

    @Getter
    @RequiredArgsConstructor
    public static class ErrorResponse {
        private final int status;
        private final String message;
        private final String code;
    }
}
