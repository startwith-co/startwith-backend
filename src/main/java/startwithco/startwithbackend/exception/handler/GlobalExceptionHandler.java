package startwithco.startwithbackend.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import startwithco.startwithbackend.exception.*;
import startwithco.startwithbackend.log.service.ExceptionLogService;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final ExceptionLogService exceptionLogService;

    @ExceptionHandler({ServerException.class})
    public ResponseEntity<ErrorResponse> handleServerException(final ServerException exception, HttpServletRequest request) {
        exceptionLogService.saveExceptionLogEntity(exception, exception.getHttpStatus(), exception.getCode(), request.getRequestURI());

        return ResponseEntity.status(exception.getHttpStatus())
                .body(new ErrorResponse(exception.getHttpStatus(), exception.getMessage(), exception.getCode()));
    }

    @ExceptionHandler({BadRequestException.class})
    public ResponseEntity<ErrorResponse> handleBadRequestException(final BadRequestException exception, HttpServletRequest request) {
        exceptionLogService.saveExceptionLogEntity(exception, exception.getHttpStatus(), exception.getCode(), request.getRequestURI());

        return ResponseEntity.status(exception.getHttpStatus())
                .body(new ErrorResponse(exception.getHttpStatus(), exception.getMessage(), exception.getCode()));
    }

    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFoundException(final NotFoundException exception, HttpServletRequest request) {
        exceptionLogService.saveExceptionLogEntity(exception, exception.getHttpStatus(), exception.getCode(), request.getRequestURI());

        return ResponseEntity.status(exception.getHttpStatus())
                .body(new ErrorResponse(exception.getHttpStatus(), exception.getMessage(), exception.getCode()));
    }

    @ExceptionHandler({UnauthorizedException.class})
    public ResponseEntity<ErrorResponse> handleNotFoundException(final UnauthorizedException exception, HttpServletRequest request) {
        exceptionLogService.saveExceptionLogEntity(exception, exception.getHttpStatus(), exception.getCode(), request.getRequestURI());

        return ResponseEntity.status(exception.getHttpStatus())
                .body(new ErrorResponse(exception.getHttpStatus(), exception.getMessage(), exception.getCode()));
    }

    @ExceptionHandler({ConflictException.class})
    public ResponseEntity<ErrorResponse> handleNotFoundException(final ConflictException exception, HttpServletRequest request) {
        exceptionLogService.saveExceptionLogEntity(exception, exception.getHttpStatus(), exception.getCode(), request.getRequestURI());

        return ResponseEntity.status(exception.getHttpStatus())
                .body(new ErrorResponse(exception.getHttpStatus(), exception.getMessage(), exception.getCode()));
    }

    @Getter
    @RequiredArgsConstructor
    public static class ErrorResponse {
        private final int httpStatus;
        private final String message;
        private final String code;
    }
}
