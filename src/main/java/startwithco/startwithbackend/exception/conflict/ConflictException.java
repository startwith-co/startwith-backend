package startwithco.startwithbackend.exception.conflict;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ConflictException extends RuntimeException {
	private final ConflictErrorResult errorResult;
}
