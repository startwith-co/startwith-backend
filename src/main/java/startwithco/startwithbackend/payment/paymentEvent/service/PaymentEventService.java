package startwithco.startwithbackend.payment.paymentEvent.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import startwithco.startwithbackend.b2b.consumer.domain.ConsumerEntity;
import startwithco.startwithbackend.b2b.consumer.repository.ConsumerRepository;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;
import startwithco.startwithbackend.b2b.vendor.repository.VendorEntityRepository;
import startwithco.startwithbackend.common.util.SELL_TYPE;
import startwithco.startwithbackend.exception.conflict.ConflictErrorResult;
import startwithco.startwithbackend.exception.conflict.ConflictException;
import startwithco.startwithbackend.exception.notFound.NotFoundErrorResult;
import startwithco.startwithbackend.exception.notFound.NotFoundException;
import startwithco.startwithbackend.exception.server.ServerErrorResult;
import startwithco.startwithbackend.exception.server.ServerException;
import startwithco.startwithbackend.payment.paymentEvent.domain.PaymentEventEntity;
import startwithco.startwithbackend.payment.paymentEvent.repository.PaymentEventEntityRepository;
import startwithco.startwithbackend.solution.solution.domain.SolutionEntity;
import startwithco.startwithbackend.solution.solution.repository.SolutionEntityRepository;

import static startwithco.startwithbackend.payment.paymentEvent.controller.request.PaymentEventRequest.*;
import static startwithco.startwithbackend.payment.paymentEvent.controller.response.PaymentEventResponse.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventService {
    private final PaymentEventEntityRepository paymentEventEntityRepository;
    private final SolutionEntityRepository solutionEntityRepository;
    private final VendorEntityRepository vendorEntityRepository;
    private final ConsumerRepository consumerRepository;

    @Transactional
    public SavePaymentEventEntityResponse savePaymentEventEntity(SavePaymentEventRequest request) {
        /*
         * [예외 처리]
         * 1. 솔루션 유효성 검사
         * 2. Vendor 유효성 검사
         * 3. Consumer 유효성 검사
         * 4. 멱등성
         * */
        SolutionEntity solutionEntity = solutionEntityRepository.findBySolutionSeq(request.solutionSeq())
                .orElseThrow(() -> new NotFoundException(NotFoundErrorResult.SOLUTION_NOT_FOUND_EXCEPTION));
        VendorEntity vendorEntity = vendorEntityRepository.findByVendorSeq(request.vendorSeq())
                .orElseThrow(() -> new NotFoundException(NotFoundErrorResult.VENDOR_NOT_FOUND_EXCEPTION));
        ConsumerEntity consumerEntity = consumerRepository.findByConsumerSeq(request.consumerSeq())
                .orElseThrow(() -> new NotFoundException(NotFoundErrorResult.CONSUMER_NOT_FOUND_EXCEPTION));

        try {
            // 1. PaymentEntity 저장
            PaymentEventEntity paymentEventEntity = PaymentEventEntity.builder()
                    .vendorEntity(vendorEntity)
                    .customerEntity(consumerEntity)
                    .solutionEntity(solutionEntity)
                    .paymentEventName(request.paymentEventName())
                    .amount(request.amount())
                    .sellType(SELL_TYPE.valueOf(request.sellType()))
                    .duration(request.duration())
                    .build();

            PaymentEventEntity savedPaymentEventEntity = paymentEventEntityRepository.savePaymentEventEntity(paymentEventEntity);

            return new SavePaymentEventEntityResponse(savedPaymentEventEntity.getPaymentEventSeq());
        } catch (DataIntegrityViolationException e) {
            log.error("PaymentEvent Service savePaymentEventEntity Method DataIntegrityViolationException-> {}", e.getMessage());

            throw new ConflictException(ConflictErrorResult.IDEMPOTENT_REQUEST_CONFLICT_EXCEPTION);
        } catch (Exception e) {
            log.error("PaymentEvent Service savePaymentEventEntity Method Exception -> {}", e.getMessage());

            throw new ServerException(ServerErrorResult.INTERNAL_SERVER_EXCEPTION);
        }
    }
}
