package startwithco.startwithbackend.log.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ExceptionLogDto {
    private LocalDateTime createdAt;
    private int httpStatus;
    private String errorCode;
    private String message;
    private String requestUri;
    private String requestBody;
    private String methodName;
}
