package startwithco.startwithbackend.b2b.client.controller.request;

import org.springframework.http.HttpStatus;
import startwithco.startwithbackend.exception.BadRequestException;
import startwithco.startwithbackend.exception.code.ExceptionCodeMapper;

import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.getCode;

public class ClientRequest {
    public record SaveClientRequest(
            Long vendorSeq,
            String clientName
    ) {
        public void validate() {
            if (vendorSeq == null) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
                );
            }
        }
    }
}
