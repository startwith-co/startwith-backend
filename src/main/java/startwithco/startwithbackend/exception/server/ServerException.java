package startwithco.startwithbackend.exception.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ServerException extends RuntimeException {
    private final ServerErrorResult errorResult;
}
