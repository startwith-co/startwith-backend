package startwithco.startwithbackend.payment.paymentEvent.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import startwithco.startwithbackend.b2b.consumer.domain.ConsumerEntity;
import startwithco.startwithbackend.b2b.consumer.repository.ConsumerRepository;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;
import startwithco.startwithbackend.b2b.vendor.repository.VendorEntityRepository;
import startwithco.startwithbackend.common.service.CommonService;
import startwithco.startwithbackend.exception.badRequest.BadRequestErrorResult;
import startwithco.startwithbackend.exception.badRequest.BadRequestException;
import startwithco.startwithbackend.exception.conflict.ConflictErrorResult;
import startwithco.startwithbackend.exception.conflict.ConflictException;
import startwithco.startwithbackend.payment.paymentEvent.util.PAYMENT_EVENT_ROUND;
import startwithco.startwithbackend.payment.paymentEvent.util.PAYMENT_EVENT_STATUS;
import startwithco.startwithbackend.solution.solution.util.SELL_TYPE;
import startwithco.startwithbackend.exception.notFound.NotFoundErrorResult;
import startwithco.startwithbackend.exception.notFound.NotFoundException;
import startwithco.startwithbackend.payment.paymentEvent.domain.PaymentEventEntity;
import startwithco.startwithbackend.payment.paymentEvent.repository.PaymentEventEntityRepository;
import startwithco.startwithbackend.solution.solution.domain.SolutionEntity;
import startwithco.startwithbackend.solution.solution.repository.SolutionEntityRepository;

import java.io.IOException;

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
    private final CommonService commonService;

    /*
     * TODO
     *  동시성 문제 처리 필요
     * */
    @Transactional
    public SavePaymentEventEntityResponse savePaymentEventEntity(
            SavePaymentEventRequest request,
            MultipartFile contractConfirmationUrl,
            MultipartFile refundPolicyUrl
    ) throws IOException {
        /*
         * [예외 처리]
         * 1. SolutionEntity 유효성 검사
         * 2. VendorEntity 유효성 검사
         * 3. ConsumerEntity 유효성 검사
         * */
        SolutionEntity solutionEntity = solutionEntityRepository.findBySolutionSeq(request.solutionSeq())
                .orElseThrow(() -> new NotFoundException(NotFoundErrorResult.SOLUTION_NOT_FOUND_EXCEPTION));
        VendorEntity vendorEntity = vendorEntityRepository.findByVendorSeq(request.vendorSeq())
                .orElseThrow(() -> new NotFoundException(NotFoundErrorResult.VENDOR_NOT_FOUND_EXCEPTION));
        ConsumerEntity consumerEntity = consumerRepository.findByConsumerSeq(request.consumerSeq())
                .orElseThrow(() -> new NotFoundException(NotFoundErrorResult.CONSUMER_NOT_FOUND_EXCEPTION));

        String s3ContractConfirmationUrl = commonService.uploadPDFFile(contractConfirmationUrl);
        String s3RefundPolicyUrl = commonService.uploadPDFFile(refundPolicyUrl);

        if (paymentEventEntityRepository.canSavePaymentEventEntity(request.consumerSeq(), request.vendorSeq(), request.solutionSeq(), SELL_TYPE.valueOf(request.sellType()))) {
            if (SELL_TYPE.valueOf(request.sellType()).equals(SELL_TYPE.SINGLE)) {
                PaymentEventEntity paymentEventEntity = PaymentEventEntity.builder()
                        .vendorEntity(vendorEntity)
                        .consumerEntity(consumerEntity)
                        .solutionEntity(solutionEntity)
                        .paymentEventName(request.paymentEventName())
                        .amount(request.amount())
                        .sellType(SELL_TYPE.valueOf(request.sellType()))
                        .amount(request.amount())
                        .duration(request.duration())
                        .paymentEventRound(PAYMENT_EVENT_ROUND.valueOf(request.paymentEventRound()))
                        .contractConfirmationUrl(s3ContractConfirmationUrl)
                        .refundPolicyUrl(s3RefundPolicyUrl)
                        .paymentEventStatus(PAYMENT_EVENT_STATUS.REQUESTED)
                        .build();

                PaymentEventEntity savedPaymentEventEntity
                        = paymentEventEntityRepository.savePaymentEventEntity(paymentEventEntity);

                return new SavePaymentEventEntityResponse(savedPaymentEventEntity.getPaymentEventSeq());
            } else if (SELL_TYPE.valueOf(request.sellType()).equals(SELL_TYPE.SUBSCRIBE)) {
                PaymentEventEntity paymentEventEntity = PaymentEventEntity.builder()
                        .vendorEntity(vendorEntity)
                        .consumerEntity(consumerEntity)
                        .solutionEntity(solutionEntity)
                        .paymentEventName(request.paymentEventName())
                        .amount(request.amount())
                        .sellType(SELL_TYPE.valueOf(request.sellType()))
                        .amount(request.amount())
                        .duration(request.duration())
                        .contractConfirmationUrl(s3ContractConfirmationUrl)
                        .refundPolicyUrl(s3RefundPolicyUrl)
                        .paymentEventStatus(PAYMENT_EVENT_STATUS.REQUESTED)
                        .build();

                PaymentEventEntity savedPaymentEventEntity
                        = paymentEventEntityRepository.savePaymentEventEntity(paymentEventEntity);

                return new SavePaymentEventEntityResponse(savedPaymentEventEntity.getPaymentEventSeq());
            } else {
                throw new BadRequestException(BadRequestErrorResult.BAD_REQUEST_EXCEPTION);
            }
        } else {
            throw new ConflictException(ConflictErrorResult.REQUEST_PAYMENT_EVENT_STATUS_CONFLICT_EXCEPTION);
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

        return new GetPaymentEventEntityResponse(
                paymentEventEntity.getPaymentEventSeq(),
                paymentEventEntity.getPaymentEventName(),
                paymentEventEntity.getSellType(),
                paymentEventEntity.getAmount(),
                paymentEventEntity.getDuration(),
                paymentEventEntity.getPaymentEventRound(),
                paymentEventEntity.getPaymentEventStatus()
        );
    }

    @Transactional
    public void deletePaymentEventEntity(DeletePaymentEventRequest request) {
        /*
         * [예외 처리]
         * 1. paymentEvent 유효성
         * */
        PaymentEventEntity paymentEventEntity = paymentEventEntityRepository.findByPaymentEventSeq(request.paymentEventSeq())
                .orElseThrow(() -> new NotFoundException(NotFoundErrorResult.PAYMENT_EVENT_NOT_FOUND_EXCEPTION));

        paymentEventEntity.updatePaymentEventStatus(PAYMENT_EVENT_STATUS.CANCELLED);
        paymentEventEntityRepository.savePaymentEventEntity(paymentEventEntity);
    }

    @Transactional(readOnly = true)
    public GetPaymentEventEntityOrderResponse getPaymentEventEntityOrder(Long paymentEventSeq) {
        /*
         * [예외 처리]
         * 1. paymentEvent 유효성
         * */
        PaymentEventEntity paymentEventEntity = paymentEventEntityRepository.findByPaymentEventSeq(paymentEventSeq)
                .orElseThrow(() -> new NotFoundException(NotFoundErrorResult.PAYMENT_EVENT_NOT_FOUND_EXCEPTION));

        SolutionEntity solutionEntity = paymentEventEntity.getSolutionEntity();
        VendorEntity vendorEntity = solutionEntity.getVendorEntity();

        Long amount = paymentEventEntity.getAmount();
        Long actualAmount = (long) (amount + amount * 0.1);

        return new GetPaymentEventEntityOrderResponse(
                solutionEntity.getRepresentImageUrl(),
                paymentEventEntity.getPaymentEventName(),
                vendorEntity.getVendorBannerImageUrl(),
                vendorEntity.getVendorName(),
                solutionEntity.getCategory(),
                solutionEntity.getDuration(),
                amount,
                actualAmount
        );
    }
}
