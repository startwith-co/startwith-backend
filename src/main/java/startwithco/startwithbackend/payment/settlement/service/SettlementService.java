package startwithco.startwithbackend.payment.settlement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import startwithco.startwithbackend.b2b.consumer.domain.ConsumerEntity;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;
import startwithco.startwithbackend.exception.ServerException;
import startwithco.startwithbackend.payment.payment.domain.PaymentEntity;
import startwithco.startwithbackend.payment.payment.repository.PaymentEntityRepository;
import startwithco.startwithbackend.payment.settlement.dto.SettlementDto;
import startwithco.startwithbackend.payment.snapshot.entity.TossPaymentDailySnapshotEntity;
import startwithco.startwithbackend.payment.snapshot.repository.TossPaymentDailySnapshotEntityRepository;
import startwithco.startwithbackend.solution.solution.domain.SolutionEntity;

import java.util.ArrayList;
import java.util.List;

import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.ExceptionType;
import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.getCode;

@Service
@RequiredArgsConstructor
public class SettlementService {
    private final PaymentEntityRepository paymentEntityRepository;
    private final TossPaymentDailySnapshotEntityRepository tossPaymentDailySnapshotEntityRepository;

    @Transactional(readOnly = true)
    public List<SettlementDto> getAllSettlementPayments(int start, int end) {
        List<PaymentEntity> paymentEntities = paymentEntityRepository.findAll(start, end);

        List<SettlementDto> response = new ArrayList<>();
        for (PaymentEntity paymentEntity : paymentEntities) {
            TossPaymentDailySnapshotEntity tossPaymentDailySnapshotEntity = tossPaymentDailySnapshotEntityRepository.findByOrderId(paymentEntity.getOrderId())
                    .orElseThrow(() -> new ServerException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "내부 서버 오류가 발생했습니다.",
                            getCode("내부 서버 오류가 발생했습니다.", ExceptionType.SERVER)
                    ));
            ConsumerEntity consumerEntity = paymentEntity.getPaymentEventEntity().getConsumerEntity();
            VendorEntity vendorEntity = paymentEntity.getPaymentEventEntity().getVendorEntity();
            SolutionEntity solutionEntity = paymentEntity.getPaymentEventEntity().getSolutionEntity();

            SettlementDto settlementDto = new SettlementDto(
                    paymentEntity.getPaymentCompletedAt(),
                    paymentEntity.getOrderId(),
                    paymentEntity.getPaymentStatus().toString(),
                    paymentEntity.getMethod(),
                    tossPaymentDailySnapshotEntity.getAmount(),
                    tossPaymentDailySnapshotEntity.getPayOutAmount(),
                    tossPaymentDailySnapshotEntity.getSettlementAmount(),
                    consumerEntity.getConsumerName(),
                    vendorEntity.getVendorName(),
                    vendorEntity.getAccountNumber(),
                    vendorEntity.getBank(),
                    solutionEntity.getSolutionName()
            );

            response.add(settlementDto);
        }

        return response;
    }

    @Transactional
    public void approveSettlement(String orderId) {
        TossPaymentDailySnapshotEntity tossPaymentDailySnapshotEntity = tossPaymentDailySnapshotEntityRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "내부 서버 오류가 발생했습니다.",
                        getCode("내부 서버 오류가 발생했습니다.", ExceptionType.SERVER)
                ));
        PaymentEntity paymentEntity = paymentEntityRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "내부 서버 오류가 발생했습니다.",
                        getCode("내부 서버 오류가 발생했습니다.", ExceptionType.SERVER)
                ));

        paymentEntity.updateSETTLEDStatus();
        paymentEntityRepository.savePaymentEntity(paymentEntity);

        tossPaymentDailySnapshotEntity.updateApproveSettlement();
        tossPaymentDailySnapshotEntityRepository.saveTossPaymentDailySnapshot(tossPaymentDailySnapshotEntity);
    }

    @Transactional
    public void cancelSettlement(String orderId) {
        TossPaymentDailySnapshotEntity tossPaymentDailySnapshotEntity = tossPaymentDailySnapshotEntityRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "내부 서버 오류가 발생했습니다.",
                        getCode("내부 서버 오류가 발생했습니다.", ExceptionType.SERVER)
                ));
        PaymentEntity paymentEntity = paymentEntityRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "내부 서버 오류가 발생했습니다.",
                        getCode("내부 서버 오류가 발생했습니다.", ExceptionType.SERVER)
                ));

        paymentEntity.updateCANCELDStatus();
        paymentEntityRepository.savePaymentEntity(paymentEntity);

        tossPaymentDailySnapshotEntity.updateDeleteSettlement();
        tossPaymentDailySnapshotEntityRepository.saveTossPaymentDailySnapshot(tossPaymentDailySnapshotEntity);
    }
}
