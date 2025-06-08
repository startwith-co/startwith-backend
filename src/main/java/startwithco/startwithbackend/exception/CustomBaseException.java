package startwithco.startwithbackend.exception;

import lombok.Getter;

@Getter
public abstract class CustomBaseException extends RuntimeException {
    private final int httpStatus;
    private final String code;

    protected CustomBaseException(String message, int httpStatus, String code) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = code;
    }
}