package startwithco.startwithbackend.exception.conflict;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ConflictErrorResult {
    CONFLICT_EXCEPTION(HttpStatus.CONFLICT.value(), "CONFLICT EXCEPTION", "CE001"),
    IDEMPOTENT_REQUEST_CONFLICT_EXCEPTION(HttpStatus.CONFLICT.value(), "IDEMPOTENT REQUEST CONFLICT EXCEPTION", "IRCE002"),
    SOLUTION_CONFLICT_EXCEPTION(HttpStatus.CONFLICT.value(), "SOLUTION CONFLICT EXCEPTION", "SCE003"),
    VENDOR_NAME_DUPLICATION_CONFLICT_EXCEPTION(HttpStatus.CONFLICT.value(), "Vendor Name Duplication Conflict Exception", "VNDCE004"),
    CONSUMER_EMAIL_DUPLICATION_CONFLICT_EXCEPTION(HttpStatus.CONFLICT.value(), "Consumer Email Duplication Conflict Exception", "CEDCE005"),
    VENDOR_EMAIL_DUPLICATION_CONFLICT_EXCEPTION(HttpStatus.CONFLICT.value(), "Vendor Email Duplication Conflict Exception", "VEDCE006"),
    INVALID_PAYMENT_EVENT_STATUS_CONFLICT_EXCEPTION(HttpStatus.CONFLICT.value(), "INVALID PAYMENT EVENT STATUS CONFLICT EXCEPTION", "IPESCE007"),
    ;

    private final int httpStatus;
    private final String message;
    private final String code;
}
