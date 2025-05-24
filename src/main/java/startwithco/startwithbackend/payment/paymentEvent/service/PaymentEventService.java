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
import startwithco.startwithbackend.payment.paymentEvent.util.PAYMENT_EVENT_STATUS;
import startwithco.startwithbackend.solution.solution.util.CATEGORY;
import startwithco.startwithbackend.solution.solution.util.SELL_TYPE;
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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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
                    .paymentEventStatus(PAYMENT_EVENT_STATUS.REQUESTED)
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

    @Transactional(readOnly = true)
    public GetPaymentEventEntityResponse getPaymentEventEntity(Long paymentEventSeq) {
        /*
         * [예외 처리]
         * 1. paymentEvent 유효성
         * */
        PaymentEventEntity paymentEventEntity = paymentEventEntityRepository.findByPaymentEventSeq(paymentEventSeq)
                .orElseThrow(() -> new NotFoundException(NotFoundErrorResult.PAYMENT_EVENT_NOT_FOUND_EXCEPTION));

        // 공통 필드
        String paymentEventName = paymentEventEntity.getPaymentEventName();
        CATEGORY category = paymentEventEntity.getSolutionEntity().getCategory();
        Long amount = paymentEventEntity.getAmount();
        SELL_TYPE sellType = paymentEventEntity.getSellType();
        Long duration = paymentEventEntity.getDuration();
        PAYMENT_EVENT_STATUS paymentEventStatus = paymentEventEntity.getPaymentEventStatus();

        // 상태에 따른 조건 필드
        Long actualDuration = null;
        LocalDateTime paymentCompletedAt = null;
        LocalDateTime developmentCompletedAt = null;
        LocalDateTime autoConfirmScheduledAt = null;

        if (paymentEventEntity.getPaymentEventStatus() == PAYMENT_EVENT_STATUS.DEVELOPED ||
                paymentEventEntity.getPaymentEventStatus() == PAYMENT_EVENT_STATUS.CONFIRMED) {

            // 조건부 필드 값 설정
            paymentCompletedAt = paymentEventEntity.getPaymentCompletedAt();
            developmentCompletedAt = paymentEventEntity.getDevelopmentCompletedAt();
            actualDuration = ChronoUnit.DAYS.between(paymentCompletedAt.toLocalDate(), developmentCompletedAt.toLocalDate());
            autoConfirmScheduledAt = paymentEventEntity.getAutoConfirmScheduledAt();
        }

        return new GetPaymentEventEntityResponse(
                paymentEventSeq,
                paymentEventName,
                category,
                amount,
                sellType,
                duration,
                paymentEventStatus,
                actualDuration,
                paymentCompletedAt,
                developmentCompletedAt,
                autoConfirmScheduledAt
        );
    }

    @Transactional
    public void modifyDevelopmentCompletedAt(ModifyDevelopmentCompletedAt request) {
        /*
         * [예외 처리]
         * 1. paymentEvent 유효성
         * */
        PaymentEventEntity paymentEventEntity = paymentEventEntityRepository.findByPaymentEventSeq(request.paymentEventSeq())
                .orElseThrow(() -> new NotFoundException(NotFoundErrorResult.PAYMENT_EVENT_NOT_FOUND_EXCEPTION));
        if(paymentEventEntity.getPaymentEventStatus() != PAYMENT_EVENT_STATUS.DEVELOPING) {
            throw new ConflictException(ConflictErrorResult.INVALID_PAYMENT_EVENT_STATUS_CONFLICT_EXCEPTION);
        }

        PaymentEventEntity updatedPaymentEventEntity = paymentEventEntity.updateDevelopmentCompletedAt();
        paymentEventEntityRepository.savePaymentEventEntity(updatedPaymentEventEntity);
    }

    @Transactional
    public void deletePaymentEventEntity(DeletePaymentEventRequest request) {
        /*
         * [예외 처리]
         * 1. paymentEvent 유효성
         * 2. REQUESTED가 아닌 경우 예외
         * */
        PaymentEventEntity paymentEventEntity = paymentEventEntityRepository.findByPaymentEventSeq(request.paymentEventSeq())
                .orElseThrow(() -> new NotFoundException(NotFoundErrorResult.PAYMENT_EVENT_NOT_FOUND_EXCEPTION));
        if (paymentEventEntity.getPaymentEventStatus() != PAYMENT_EVENT_STATUS.REQUESTED) {
            throw new ConflictException(ConflictErrorResult.INVALID_PAYMENT_EVENT_STATUS_CONFLICT_EXCEPTION);
        }

        PaymentEventEntity updatedPaymentEventEntity = paymentEventEntity.updatePaymentEventStatus(PAYMENT_EVENT_STATUS.CANCELED);
        paymentEventEntityRepository.savePaymentEventEntity(updatedPaymentEventEntity);
    }
}
