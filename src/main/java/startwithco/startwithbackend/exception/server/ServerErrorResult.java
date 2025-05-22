package startwithco.startwithbackend.exception.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ServerErrorResult {
    INTERNAL_SERVER_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL SERVER EXCEPTION", "ISE001");

    private final int httpStatus;
    private final String message;
    private final String code;
}
