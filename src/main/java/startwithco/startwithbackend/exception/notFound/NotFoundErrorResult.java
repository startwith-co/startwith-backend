package startwithco.startwithbackend.exception.notFound;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NotFoundErrorResult {
    NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND.value(), "NOT FOUND EXCEPTION", "NFE001"),
    VENDOR_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND.value(), "VENDOR NOT FOUND EXCEPTION", "VNFE002"),
    SOLUTION_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND.value(), "SOLUTION NOT FOUND EXCEPTION", "SNFE003"),
    CONSUMER_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND.value(), "CONSUMER NOT FOUND EXCEPTION", "CNFE004"),
    ;

    private final int httpStatus;
    private final String message;
    private final String code;
}
