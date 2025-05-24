package startwithco.startwithbackend.payment.paymentEvent.util;

public enum PAYMENT_EVENT_STATUS {
    REQUESTED,           // 결제 요청 (예: 알림 버튼에 “결제 요청”)
    CANCELED,            // 결제 요청 취소
    DEVELOPING,          // 개발 중
    DEVELOPED,           // 개발 완료 (예: 완료 알림)
    CONFIRMED            // 구매 확정
}