package startwithco.startwithbackend.exception.unauthorized;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UnauthorizedErrorResult {
    UNAUTHORIZED_EXCEPTION(HttpStatus.UNAUTHORIZED.value(), "UNAUTHORIZED EXCEPTION", "UAE001");

    private final int httpStatus;
    private final String message;
    private final String code;
}
