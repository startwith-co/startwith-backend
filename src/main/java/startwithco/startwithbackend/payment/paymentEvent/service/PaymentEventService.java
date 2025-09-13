package startwithco.startwithbackend.payment.paymentEvent.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import startwithco.startwithbackend.b2b.consumer.domain.ConsumerEntity;
import startwithco.startwithbackend.b2b.consumer.repository.ConsumerRepository;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;
import startwithco.startwithbackend.b2b.vendor.repository.VendorEntityRepository;
import startwithco.startwithbackend.common.service.CommonService;
import startwithco.startwithbackend.exception.ConflictException;
import startwithco.startwithbackend.payment.payment.domain.PaymentEntity;
import startwithco.startwithbackend.payment.payment.repository.PaymentEntityRepository;
import startwithco.startwithbackend.payment.payment.util.PAYMENT_STATUS;
import startwithco.startwithbackend.solution.solution.util.CATEGORY;
import startwithco.startwithbackend.exception.NotFoundException;
import startwithco.startwithbackend.payment.paymentEvent.domain.PaymentEventEntity;
import startwithco.startwithbackend.payment.paymentEvent.repository.PaymentEventEntityRepository;
import startwithco.startwithbackend.solution.solution.domain.SolutionEntity;
import startwithco.startwithbackend.solution.solution.repository.SolutionEntityRepository;

import java.util.List;

import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.*;
import static startwithco.startwithbackend.payment.paymentEvent.controller.request.PaymentEventRequest.*;
import static startwithco.startwithbackend.payment.paymentEvent.controller.response.PaymentEventResponse.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventService {
    private final PaymentEventEntityRepository paymentEventEntityRepository;
    private final PaymentEntityRepository paymentEntityRepository;
    private final SolutionEntityRepository solutionEntityRepository;
    private final VendorEntityRepository vendorEntityRepository;
    private final ConsumerRepository consumerRepository;
    private final CommonService commonService;

    /*
     * TODO
     *  동시성 문제 처리 필요
     * */
    @Transactional
    public SavePaymentEventEntityResponse savePaymentEventEntity(SavePaymentEventRequest request, MultipartFile contractConfirmationUrl, MultipartFile refundPolicyUrl) {
        SolutionEntity solutionEntity = solutionEntityRepository.findByVendorSeqAndCategory(request.vendorSeq(), CATEGORY.valueOf(request.category()))
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 솔루션입니다.",
                        getCode("존재하지 않는 솔루션입니다.", ExceptionType.NOT_FOUND)
                ));
        VendorEntity vendorEntity = vendorEntityRepository.findByVendorSeq(request.vendorSeq())
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 벤더 기업입니다.",
                        getCode("존재하지 않는 벤더 기업입니다.", ExceptionType.NOT_FOUND)
                ));
        ConsumerEntity consumerEntity = consumerRepository.findByConsumerSeq(request.consumerSeq())
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 수요 기업입니다.",
                        getCode("존재하지 않는 수요 기업입니다.", ExceptionType.NOT_FOUND)
                ));

        String s3ContractConfirmationUrl = commonService.uploadPDFFile(contractConfirmationUrl);
        String s3RefundPolicyUrl = commonService.uploadPDFFile(refundPolicyUrl);
        Long amount = solutionEntity.getAmount();
        Long tax = (long) (amount * 0.1);
        Long actualAmount = amount + tax;

        PaymentEventEntity paymentEventEntity = PaymentEventEntity.builder()
                .vendorEntity(vendorEntity)
                .consumerEntity(consumerEntity)
                .solutionEntity(solutionEntity)
                .paymentEventName(request.paymentEventName())
                .amount(amount)
                .tax(tax)
                .actualAmount(actualAmount)
                .contractConfirmationUrl(s3ContractConfirmationUrl)
                .refundPolicyUrl(s3RefundPolicyUrl)
                .paymentEventUniqueType(request.paymentEventUniqueType())
                .build();

        try {
            paymentEventEntityRepository.savePaymentEventEntity(paymentEventEntity);

            return new SavePaymentEventEntityResponse(paymentEventEntity.getPaymentEventSeq(), paymentEventEntity.getPaymentEventUniqueType());
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(
                    HttpStatus.CONFLICT.value(),
                    "동시성 저장은 불가능합니다.",
                    getCode("동시성 저장은 불가능합니다.", ExceptionType.CONFLICT)
            );
        }
    }

    @Transactional(readOnly = true)
    public Object getPaymentEventEntity(String paymentEventUniqueType) {
        PaymentEventEntity paymentEventEntity = paymentEventEntityRepository.findByPaymentEventUniqueType(paymentEventUniqueType)
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 결제 요청입니다.",
                        getCode("존재하지 않는 결제 요청입니다.", ExceptionType.NOT_FOUND)
                ));

        PaymentEntity paymentEntity = paymentEntityRepository.findByPaymentEventUniqueType(paymentEventUniqueType)
                .orElse(null);

        if (paymentEntity != null && (paymentEntity.getPaymentStatus() == PAYMENT_STATUS.DONE || paymentEntity.getPaymentStatus() == PAYMENT_STATUS.SETTLED)) {
            return new GetCONFIRMEDPaymentEventEntityResponse(
                    paymentEventEntity.getPaymentEventSeq(),
                    paymentEventEntity.getPaymentEventName(),
                    paymentEntity.getOrderId(),
                    paymentEventEntity.getSolutionEntity().getCategory(),
                    paymentEventEntity.getAmount(),
                    paymentEventEntity.getCreatedAt(),
                    paymentEntity.getDueDate()
            );
        } else {
            return new GetREQUESTEDPaymentEventEntityResponse(
                    paymentEventEntity.getPaymentEventSeq(),
                    paymentEventEntity.getPaymentEventName(),
                    paymentEventEntity.getSolutionEntity().getCategory(),
                    paymentEventEntity.getAmount(),
                    paymentEventEntity.getContractConfirmationUrl(),
                    paymentEventEntity.getRefundPolicyUrl(),
                    paymentEventEntity.getCreatedAt(),
                    paymentEventEntity.getSolutionEntity().getDeleted()
            );
        }
    }

    @Transactional(readOnly = true)
    public GetPaymentEventEntityOrderResponse getPaymentEventEntityOrder(Long paymentEventSeq) {
        PaymentEventEntity paymentEventEntity = paymentEventEntityRepository.findByPaymentEventSeq(paymentEventSeq)
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 결제 요청입니다.",
                        getCode("존재하지 않는 결제 요청입니다.", ExceptionType.NOT_FOUND)
                ));

        SolutionEntity solutionEntity = paymentEventEntity.getSolutionEntity();
        if (solutionEntity.getDeleted()) {
            throw new NotFoundException(
                    HttpStatus.NOT_FOUND.value(),
                    "존재하지 않는 솔루션입니다.",
                    getCode("존재하지 않는 솔루션입니다.", ExceptionType.NOT_FOUND)
            );
        }

        VendorEntity vendorEntity = solutionEntity.getVendorEntity();
        ConsumerEntity consumerEntity = paymentEventEntity.getConsumerEntity();

        return new GetPaymentEventEntityOrderResponse(
                paymentEventEntity.getPaymentEventSeq(),
                paymentEventEntity.getPaymentEventName(),
                solutionEntity.getCategory(),
                vendorEntity.getVendorName(),
                vendorEntity.getProfileImage(),
                solutionEntity.getRepresentImageUrl(),
                paymentEventEntity.getAmount(),
                paymentEventEntity.getTax(),
                paymentEventEntity.getActualAmount(),
                consumerEntity.getConsumerSeq(),
                consumerEntity.getConsumerName(),
                consumerEntity.getPhoneNumber(),
                consumerEntity.getEmail()
        );
    }

    @Transactional(readOnly = true)
    public boolean getPaymentEventConflict(Long vendorSeq, Long consumerSeq, CATEGORY category) {
        VendorEntity vendorEntity = vendorEntityRepository.findByVendorSeq(vendorSeq)
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 벤더 기업입니다.",
                        getCode("존재하지 않는 벤더 기업입니다.", ExceptionType.NOT_FOUND)
                ));
        ConsumerEntity consumerEntity = consumerRepository.findByConsumerSeq(consumerSeq)
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 수요 기업입니다.",
                        getCode("존재하지 않는 수요 기업입니다.", ExceptionType.NOT_FOUND)
                ));
        SolutionEntity solutionEntity = solutionEntityRepository.findByVendorSeqAndCategory(vendorSeq, category)
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "해당 기업이 작성한 카테고리 솔루션이 존재하지 않습니다.",
                        getCode("해당 기업이 작성한 카테고리 솔루션이 존재하지 않습니다.", ExceptionType.NOT_FOUND)
                ));

        return paymentEntityRepository.existsConflictPaymentEntity(vendorEntity, consumerEntity, solutionEntity);
    }
}
