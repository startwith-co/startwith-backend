package startwithco.startwithbackend.b2b.consumer.controller.request;

import startwithco.startwithbackend.exception.badRequest.BadRequestErrorResult;
import startwithco.startwithbackend.exception.badRequest.BadRequestException;

public class ConsumerRequest {

    public record SaveConsumerRequest(
            String consumerName,
            String password,
            String email,
            String industry
    ) {

        public void validateSaveConsumerRequest(SaveConsumerRequest request){

            if (request.consumerName() == null || request.email() == null
                    || request.password() == null || request.industry() == null) {
                throw new BadRequestException(BadRequestErrorResult.BAD_REQUEST_EXCEPTION);
            }

        }
    }

    public record LoginRequest(
            String email,
            String password
    ) {
    }



}

