package startwithco.startwithbackend.exception.unauthorized;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UnauthorizedException extends RuntimeException {
    private final UnauthorizedErrorResult errorResult;
}
