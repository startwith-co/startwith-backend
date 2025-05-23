package startwithco.startwithbackend.exception.notFound;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NotFoundErrorResult {
    NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND.value(), "NOT FOUND EXCEPTION", "NFE001"),
    VENDOR_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND.value(), "VENDOR NOT FOUND EXCEPTION", "NFE002"),
    SOLUTION_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND.value(), "SOLUTION NOT FOUND EXCEPTION", "SNFE003"),
    ;

    private final int httpStatus;
    private final String message;
    private final String code;
}
