package startwithco.startwithbackend.payment.paymentEvent.util;

public enum PAYMENT_EVENT_STATUS {
    REQUESTED,           // 결제 요청
    CANCELED,            // 결제 요청 취소
    DEVELOPING,          // 개발 중
    DEVELOPED,           // 개발 완료
    CONFIRMED,           // 구매 확정
    SETTLEMENT_COMPLETED // 정산 완료
}