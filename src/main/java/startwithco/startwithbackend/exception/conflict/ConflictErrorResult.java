package startwithco.startwithbackend.exception.conflict;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ConflictErrorResult {
    CONFLICT_EXCEPTION(HttpStatus.CONFLICT.value(), "CONFLICT EXCEPTION", "CE001"),
    IDEMPOTENT_REQUEST_CONFLICT_EXCEPTION(HttpStatus.CONFLICT.value(), "IDEMPOTENT REQUEST CONFLICT EXCEPTION", "IRCE002");;

    private final int httpStatus;
    private final String message;
    private final String code;
}
