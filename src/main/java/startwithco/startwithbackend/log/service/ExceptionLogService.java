package startwithco.startwithbackend.log.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import startwithco.startwithbackend.log.domain.ExceptionLogEntity;
import startwithco.startwithbackend.log.dto.ExceptionLogDto;
import startwithco.startwithbackend.log.repository.ExceptionLogEntityRepository;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExceptionLogService {
    private final ExceptionLogEntityRepository exceptionLogEntityRepository;

    public void saveExceptionLogEntity(Exception exception, int status, String code, String uri) {
        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        StackTraceElement[] stackTrace = exception.getStackTrace();
        String methodName = stackTrace.length > 0 ? stackTrace[0].toString() : "UNKNOWN";

        ExceptionLogEntity exceptionLogEntity = ExceptionLogEntity.builder()
                .exceptionType(exception.getClass().getSimpleName())
                .httpStatus(status)
                .errorCode(code)
                .message(exception.getMessage())
                .requestUri(uri)
                .methodName(methodName)
                .build();

        exceptionLogEntityRepository.saveExceptionLogEntity(exceptionLogEntity);
    }

    public List<ExceptionLogDto> getAllExceptionLogEntity(int start, int end) {
        return exceptionLogEntityRepository.findAll(start, end).stream()
                .map(log -> new ExceptionLogDto(
                        log.getCreatedAt(),
                        log.getExceptionType(),
                        log.getHttpStatus(),
                        log.getErrorCode(),
                        log.getMessage(),
                        log.getRequestUri(),
                        log.getMethodName()
                ))
                .collect(Collectors.toList());
    }
}
