package startwithco.startwithbackend.b2b.consumer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import startwithco.startwithbackend.b2b.consumer.controller.request.ConsumerRequest;
import startwithco.startwithbackend.b2b.consumer.domain.ConsumerEntity;
import startwithco.startwithbackend.b2b.consumer.repository.ConsumerRepository;
import startwithco.startwithbackend.exception.conflict.ConflictErrorResult;
import startwithco.startwithbackend.exception.conflict.ConflictException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumerService {

    private final ConsumerRepository consumerRepository;
    private final BCryptPasswordEncoder encoder;


    @Transactional
    public void saveConsumer(ConsumerRequest.SaveConsumerRequest request) {

        // 유효성 검사
        consumerRepository.isDuplicatedConsumerName(request.consumerName())
                .ifPresent(entity -> {
            throw new ConflictException(ConflictErrorResult.CONSUMER_NAME_DUPLICATION_CONFLICT_EXCEPTION);
        });;

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
}
