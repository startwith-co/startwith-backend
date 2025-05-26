package startwithco.startwithbackend.b2b.consumer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import startwithco.startwithbackend.b2b.consumer.controller.request.ConsumerRequest;
import startwithco.startwithbackend.b2b.consumer.controller.response.ConsumerResponse;
import startwithco.startwithbackend.b2b.consumer.domain.ConsumerEntity;
import startwithco.startwithbackend.b2b.consumer.repository.ConsumerRepository;
import startwithco.startwithbackend.exception.conflict.ConflictErrorResult;
import startwithco.startwithbackend.exception.conflict.ConflictException;
import startwithco.startwithbackend.exception.notFound.NotFoundErrorResult;
import startwithco.startwithbackend.exception.notFound.NotFoundException;
import startwithco.startwithbackend.payment.paymentEvent.repository.PaymentEventEntityRepository;

import static startwithco.startwithbackend.b2b.consumer.controller.response.ConsumerResponse.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumerService {

    private final ConsumerRepository consumerRepository;
    private final PaymentEventEntityRepository paymentEventEntityRepository;
    private final BCryptPasswordEncoder encoder;


    @Transactional
    public void saveConsumer(ConsumerRequest.SaveConsumerRequest request) {

        // 유효성 검사
        consumerRepository.findByEmail(request.email())
                .ifPresent(entity -> {
                    throw new ConflictException(ConflictErrorResult.CONSUMER_EMAIL_DUPLICATION_CONFLICT_EXCEPTION);
                });

        try {
            ConsumerEntity consumerEntity = ConsumerEntity.builder()
                    .consumerName(request.consumerName())
                    .encodedPassword(encoder.encode(request.password()))
                    .email(request.email())
                    .industry(request.industry())
                    .build();

            consumerRepository.save(consumerEntity);

        } catch (DataIntegrityViolationException e) {
            log.error("Solution Service saveSolutionEntity Method DataIntegrityViolationException-> {}", e.getMessage());
            throw new ConflictException(ConflictErrorResult.IDEMPOTENT_REQUEST_CONFLICT_EXCEPTION);
        }
    }

    @Transactional(readOnly = true)
    public ConsumerDetailResponse getConsumerDetails(Long consumerSeq) {
        /*
         * [예외 처리]
         * 1. 존재하지 않는 소비자: 404 CONSUMER_NOT_FOUND_EXCEPTION
         */
        consumerRepository.findByConsumerSeq(consumerSeq)
                .orElseThrow(() -> new NotFoundException(NotFoundErrorResult.CONSUMER_NOT_FOUND_EXCEPTION));

        Long DEVELOPING = paymentEventEntityRepository.countDEVELOPEDByConsumerSeq(consumerSeq);
        Long DEVELOPED = paymentEventEntityRepository.countDEVELOPEDByConsumerSeq(consumerSeq);
        Long CONFIRMED = paymentEventEntityRepository.countCONFIRMEDByConsumerSeq(consumerSeq);

        return new ConsumerDetailResponse(DEVELOPING, DEVELOPED, CONFIRMED);
    }
}
