package startwithco.startwithbackend.exception.badRequest;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BadRequestErrorResult {
    BAD_REQUEST_EXCEPTION(HttpStatus.BAD_REQUEST.value(), "BAD REQUEST EXCEPTION", "BRE001"),
    AMOUNT_MISMATCH_BAD_REQUEST_EXCEPTION(HttpStatus.BAD_REQUEST.value(), "AMOUNT MISMATCH BAD REQUEST EXCEPTION", "AMBRE002")

    ;

    private final int httpStatus;
    private final String message;
    private final String code;
}
