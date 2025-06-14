package startwithco.startwithbackend.exception;

import lombok.Getter;

@Getter
public class NotFoundException  extends CustomBaseException {
    public NotFoundException(int httpStatus, String message, String code) {
        super(message, httpStatus, code);
    }
}
