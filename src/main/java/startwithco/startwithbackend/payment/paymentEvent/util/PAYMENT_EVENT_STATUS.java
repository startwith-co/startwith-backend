package startwithco.startwithbackend.payment.paymentEvent.util;

public enum PAYMENT_EVENT_STATUS {
    REQUESTED,  // 결제 요청
    CANCELLED,  // 결제 요청 취소
    CONFIRMED,  // 구매 확정
    SETTLED,    // 정산 완료
    FAILED      // PG사 연동 실패
    ;
}
