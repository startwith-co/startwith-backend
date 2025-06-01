package startwithco.startwithbackend.payment.paymentEvent.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import startwithco.startwithbackend.b2b.consumer.domain.ConsumerEntity;
import startwithco.startwithbackend.b2b.consumer.repository.ConsumerRepository;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;
import startwithco.startwithbackend.b2b.vendor.repository.VendorEntityRepository;
import startwithco.startwithbackend.common.service.CommonService;
import startwithco.startwithbackend.exception.BadRequestException;
import startwithco.startwithbackend.exception.ConflictException;
import startwithco.startwithbackend.exception.ServerException;
import startwithco.startwithbackend.payment.payment.repository.PaymentEntityRepository;
import startwithco.startwithbackend.payment.payment.util.PAYMENT_STATUS;
import startwithco.startwithbackend.payment.paymentEvent.util.PAYMENT_EVENT_STATUS;
import startwithco.startwithbackend.solution.solution.util.CATEGORY;
import startwithco.startwithbackend.exception.NotFoundException;
import startwithco.startwithbackend.payment.paymentEvent.domain.PaymentEventEntity;
import startwithco.startwithbackend.payment.paymentEvent.repository.PaymentEventEntityRepository;
import startwithco.startwithbackend.solution.solution.domain.SolutionEntity;
import startwithco.startwithbackend.solution.solution.repository.SolutionEntityRepository;

import java.util.UUID;

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
        if (!paymentEventEntityRepository.canSavePaymentEventEntity(request.consumerSeq(), request.vendorSeq(), solutionEntity.getSolutionSeq())) {
            throw new ConflictException(
                    HttpStatus.CONFLICT.value(),
                    "이미 동일한 솔루션에 대한 결제 요청이 진행 중입니다.",
                    getCode("이미 동일한 솔루션에 대한 결제 요청이 진행 중입니다.", ExceptionType.CONFLICT)
            );
        }

        String s3ContractConfirmationUrl = commonService.uploadPDFFile(contractConfirmationUrl);
        String s3RefundPolicyUrl = commonService.uploadPDFFile(refundPolicyUrl);
        String orderId = UUID.randomUUID().toString();
        Long tax = (long) (request.amount() * 0.1);
        Long actualAmount = request.amount() + tax;

        PaymentEventEntity paymentEventEntity = PaymentEventEntity.builder()
                .vendorEntity(vendorEntity)
                .consumerEntity(consumerEntity)
                .solutionEntity(solutionEntity)
                .paymentEventName(request.paymentEventName())
                .amount(request.amount())
                .tax(tax)
                .actualAmount(actualAmount)
                .contractConfirmationUrl(s3ContractConfirmationUrl)
                .refundPolicyUrl(s3RefundPolicyUrl)
                .paymentEventStatus(PAYMENT_EVENT_STATUS.REQUESTED)
                .orderId(orderId)
                .build();

        paymentEventEntityRepository.savePaymentEventEntity(paymentEventEntity);

        return new SavePaymentEventEntityResponse(paymentEventEntity.getPaymentEventSeq());
    }

    @Transactional(readOnly = true)
    public Object getPaymentEventEntity(Long paymentEventSeq) {
        PaymentEventEntity paymentEventEntity = paymentEventEntityRepository.findByPaymentEventSeq(paymentEventSeq)
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 결제 요청입니다.",
                        getCode("존재하지 않는 결제 요청입니다.", ExceptionType.NOT_FOUND)
                ));

        if (paymentEventEntity.getPaymentEventStatus() == PAYMENT_EVENT_STATUS.CONFIRMED ||
                paymentEventEntity.getPaymentEventStatus() == PAYMENT_EVENT_STATUS.SETTLED) {
            paymentEntityRepository.findSUCCESSByPaymentEventSeq(paymentEventSeq)
                    .orElseThrow(() -> new ServerException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "구매 확정, 정산 완료 결제 요청이지만 결제 승인된 정보가 없습니다.",
                            getCode("구매 확정, 정산 완료 결제 요청이지만 결제 승인된 정보가 없습니다.", ExceptionType.SERVER)
                    ));

            return new GetCONFIRMEDPaymentEventEntityResponse(
                    paymentEventEntity.getPaymentEventSeq(),
                    paymentEventEntity.getPaymentEventName(),
                    paymentEventEntity.getSolutionEntity().getCategory(),
                    paymentEventEntity.getActualAmount(),
                    paymentEventEntity.getPaymentEventStatus(),
                    paymentEventEntity.getCreatedAt()
            );
        } else {
            return new GetREQUESTEDPaymentEventEntityResponse(
                    paymentEventEntity.getPaymentEventSeq(),
                    paymentEventEntity.getPaymentEventName(),
                    paymentEventEntity.getSolutionEntity().getCategory(),
                    paymentEventEntity.getAmount(),
                    paymentEventEntity.getPaymentEventStatus(),
                    paymentEventEntity.getContractConfirmationUrl(),
                    paymentEventEntity.getRefundPolicyUrl(),
                    paymentEventEntity.getOrderId(),
                    paymentEventEntity.getCreatedAt()
            );
        }
    }

    @Transactional
    public void deletePaymentEventEntity(DeletePaymentEventRequest request) {
        PaymentEventEntity paymentEventEntity = paymentEventEntityRepository.findByPaymentEventSeq(request.paymentEventSeq())
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 결제 요청입니다.",
                        getCode("존재하지 않는 결제 요청입니다.", ExceptionType.NOT_FOUND)
                ));
        paymentEntityRepository.findSUCCESSByPaymentEventSeq(request.paymentEventSeq())
                .ifPresent(entity -> {
                    throw new BadRequestException(
                            HttpStatus.BAD_REQUEST.value(),
                            "이미 결제 승인된 결제 요청입니다.",
                            getCode("이미 결제 승인된 결제 요청입니다.", ExceptionType.BAD_REQUEST)
                    );
                });
        paymentEntityRepository.findINPROGRESSByPaymentEventSeq(request.paymentEventSeq())
                .ifPresent(paymentEntity -> {
                    paymentEntity.updatePaymentStatus(PAYMENT_STATUS.CANCELLED);
                    paymentEntityRepository.savePaymentEntity(paymentEntity);
                });

        paymentEventEntity.updatePaymentEventStatus(PAYMENT_EVENT_STATUS.CANCELLED);
        paymentEventEntityRepository.savePaymentEventEntity(paymentEventEntity);
    }

    @Transactional(readOnly = true)
    public GetPaymentEventEntityOrderResponse getPaymentEventEntityOrder(Long paymentEventSeq) {
        PaymentEventEntity paymentEventEntity = paymentEventEntityRepository.findByPaymentEventSeq(paymentEventSeq)
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 결제 요청입니다.",
                        getCode("존재하지 않는 결제 요청입니다.", ExceptionType.NOT_FOUND)
                ));
        if (paymentEventEntity.getPaymentEventStatus() != PAYMENT_EVENT_STATUS.REQUESTED) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "이미 결제 승인된 결제 요청입니다.",
                    getCode("이미 결제 승인된 결제 요청입니다.", ExceptionType.BAD_REQUEST)
            );
        }

        SolutionEntity solutionEntity = paymentEventEntity.getSolutionEntity();
        VendorEntity vendorEntity = solutionEntity.getVendorEntity();
        ConsumerEntity consumerEntity = paymentEventEntity.getConsumerEntity();

        return new GetPaymentEventEntityOrderResponse(
                paymentEventEntity.getPaymentEventSeq(),
                paymentEventEntity.getOrderId(),
                paymentEventEntity.getPaymentEventName(),
                solutionEntity.getCategory(),
                vendorEntity.getVendorName(),
                vendorEntity.getVendorBannerImageUrl(),
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
}
