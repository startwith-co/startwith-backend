package startwithco.startwithbackend.exception;

import lombok.Getter;

@Getter
public class ServerException extends CustomBaseException {
    public ServerException(int httpStatus, String message, String code) {
        super(message, httpStatus, code);
    }
}
