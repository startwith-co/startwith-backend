package startwithco.startwithbackend.exception.notFound;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NotFoundException extends RuntimeException {
    private final NotFoundErrorResult errorResult;
}
