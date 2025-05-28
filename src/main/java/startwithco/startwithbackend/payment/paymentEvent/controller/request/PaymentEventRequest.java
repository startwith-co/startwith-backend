package startwithco.startwithbackend.payment.paymentEvent.controller.request;

import org.springframework.http.HttpStatus;
import startwithco.startwithbackend.exception.code.ExceptionCodeMapper;
import startwithco.startwithbackend.solution.solution.util.CATEGORY;
import startwithco.startwithbackend.exception.BadRequestException;

import static io.micrometer.common.util.StringUtils.isBlank;
import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.getCode;

public class PaymentEventRequest {
    public record SavePaymentEventRequest(
            Long consumerSeq,
            Long vendorSeq,
            String category,
            String paymentEventName,
            Long amount
    ) {
        public void validate() {
            if (consumerSeq == null ||
                    vendorSeq == null ||
                    isBlank(paymentEventName) ||
                    isBlank(category) ||
                    amount == null) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
                );
            }

            if (amount < 0) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
                );
            }

            try {
                CATEGORY.valueOf(category.toUpperCase());
            } catch (Exception e) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
                );
            }
        }
    }

    public record DeletePaymentEventRequest(
            Long paymentEventSeq
    ) {
        public void validate() {
            if (paymentEventSeq == null) {
                throw new BadRequestException(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 오류입니다.",
                        getCode("요청 데이터 오류입니다.", ExceptionCodeMapper.ExceptionType.BAD_REQUEST)
                );
            }
        }
    }
}
