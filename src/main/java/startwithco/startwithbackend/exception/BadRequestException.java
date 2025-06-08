package startwithco.startwithbackend.exception;

import lombok.Getter;

@Getter
public class BadRequestException  extends CustomBaseException {
    public BadRequestException(int httpStatus, String message, String code) {
        super(message, httpStatus, code);
    }
}
