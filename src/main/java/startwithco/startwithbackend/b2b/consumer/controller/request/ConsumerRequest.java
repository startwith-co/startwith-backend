package startwithco.startwithbackend.b2b.consumer.controller.request;

import org.springframework.http.HttpStatus;
import startwithco.startwithbackend.b2b.vendor.controller.request.VendorRequest;
import startwithco.startwithbackend.exception.BadRequestException;
import startwithco.startwithbackend.exception.code.ExceptionCodeMapper;

import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.getCode;

public class ConsumerRequest {
    public record SaveConsumerRequest(
            String consumerName,
            String phoneNum,
            String email,
            String password,
            String confirmPassword,
            String industry
    ) {

        public void validateSaveConsumerRequest(SaveConsumerRequest request){

            if (request.consumerName() == null || request.email() == null || request.phoneNum() == null
                    || request.password() == null || request.confirmPassword() == null || request.industry() == null) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
                );
            }

            if(!request.password().equals(request.confirmPassword())) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
                );
            }

        }
    }

    public record LoginConsumerRequest (
            String email,
            String password
    ) {
        public void validateLoginConsumerRequest(LoginConsumerRequest request) {

            if (request.email == null || request.password == null) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
                );
            }
        }
    }
}
