package startwithco.startwithbackend.b2b.consumer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import startwithco.startwithbackend.b2b.consumer.domain.ConsumerEntity;
import startwithco.startwithbackend.b2b.consumer.repository.ConsumerRepository;
import startwithco.startwithbackend.exception.ConflictException;

import static startwithco.startwithbackend.b2b.consumer.controller.request.ConsumerRequest.*;
import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.*;

@Service
@RequiredArgsConstructor
public class ConsumerService {
    private final ConsumerRepository consumerRepository;
    private final BCryptPasswordEncoder encoder;

    @Transactional
    public void saveConsumer(SaveConsumerRequest request) {
        // 유효성 검사
        consumerRepository.findByEmail(request.email())
                .ifPresent(entity -> {
                    throw new ConflictException(
                            HttpStatus.CONFLICT.value(),
                            "중복된 이메일입니다.",
                            getCode("중복된 이메일입니다.", ExceptionType.CONFLICT)
                    );
                });

        try {
            ConsumerEntity consumerEntity = ConsumerEntity.builder()
                    .consumerName(request.consumerName())
                    .encodedPassword(encoder.encode(request.password()))
                    .phoneNum(request.phoneNum())
                    .email(request.email())
                    .industry(request.industry())
                    .build();

            consumerRepository.save(consumerEntity);

        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(
                    HttpStatus.CONFLICT.value(),
                    "동시성 저장은 불가능합니다.",
                    getCode("동시성 저장은 불가능합니다.", ExceptionType.CONFLICT)
            );
        }
    }
}
