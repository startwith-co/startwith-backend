package startwithco.startwithbackend.b2b.consumer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import startwithco.startwithbackend.b2b.consumer.domain.ConsumerEntity;
import startwithco.startwithbackend.b2b.consumer.repository.ConsumerRepository;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;
import startwithco.startwithbackend.exception.ConflictException;
import startwithco.startwithbackend.exception.NotFoundException;
import startwithco.startwithbackend.exception.ServerException;
import startwithco.startwithbackend.payment.payment.domain.PaymentEntity;
import startwithco.startwithbackend.payment.payment.repository.PaymentEntityRepository;
import startwithco.startwithbackend.payment.paymentEvent.domain.PaymentEventEntity;
import startwithco.startwithbackend.payment.paymentEvent.repository.PaymentEventEntityRepository;
import startwithco.startwithbackend.solution.review.repository.SolutionReviewEntityRepository;
import startwithco.startwithbackend.solution.solution.domain.SolutionEntity;

import java.util.ArrayList;
import java.util.List;

import static startwithco.startwithbackend.b2b.consumer.controller.request.ConsumerRequest.*;
import static startwithco.startwithbackend.b2b.consumer.controller.response.ConsumerResponse.*;
import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.*;

@Service
@RequiredArgsConstructor
public class ConsumerService {
    private final ConsumerRepository consumerRepository;
    private final PaymentEventEntityRepository paymentEventEntityRepository;
    private final PaymentEntityRepository paymentEntityRepository;
    private final SolutionReviewEntityRepository solutionReviewEntityRepository;

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
                    .phoneNumber(request.phoneNum())
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

    @Transactional(readOnly = true)
    public List<GetConsumerDashboardResponse> getConsumerDashboard(Long consumerSeq) {
        consumerRepository.findByConsumerSeq(consumerSeq)
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 수요 기업입니다.",
                        getCode("존재하지 않는 수요 기업입니다.", ExceptionType.NOT_FOUND)
                ));

        List<PaymentEventEntity> paymentEventEntities = paymentEventEntityRepository.findAllByConsumerSeq(consumerSeq);
        List<GetConsumerDashboardResponse> response = new ArrayList<>();
        for (PaymentEventEntity paymentEventEntity : paymentEventEntities) {
            SolutionEntity solutionEntity = paymentEventEntity.getSolutionEntity();
            VendorEntity vendorEntity = paymentEventEntity.getVendorEntity();
            PaymentEntity paymentEntity = paymentEntityRepository.findSUCCESSByPaymentEventSeq(paymentEventEntity.getPaymentEventSeq())
                    .orElseThrow(() -> new ServerException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "구매 확정, 정산 완료 결제 요청이지만 결제 승인된 정보가 없습니다.",
                            getCode("구매 확정, 정산 완료 결제 요청이지만 결제 승인된 정보가 없습니다.", ExceptionType.SERVER)
                    ));

            response.add(new GetConsumerDashboardResponse(
                    paymentEventEntity.getPaymentEventStatus(),
                    solutionEntity.getSolutionSeq(),
                    solutionEntity.getSolutionName(),
                    solutionEntity.getRepresentImageUrl(),
                    solutionReviewEntityRepository.existsByConsumerSeqAndSolutionSeq(consumerSeq, solutionEntity.getSolutionSeq()),
                    vendorEntity.getVendorSeq(),
                    vendorEntity.getVendorName(),
                    paymentEntity.getPaymentCompletedAt(),
                    paymentEntity.getMethod(),
                    paymentEntity.getAmount()
            ));
        }

        return response;
    }

    public void validateEmail(String email) {

        consumerRepository.findByEmail(email)
                .ifPresent(entity -> {
                    throw new ConflictException(
                            HttpStatus.CONFLICT.value(),
                            "이미 가입한 이메일 입니다.",
                            getCode("이미 가입한 이메일 입니다.", ExceptionType.CONFLICT)
                    );
                });
    }
}