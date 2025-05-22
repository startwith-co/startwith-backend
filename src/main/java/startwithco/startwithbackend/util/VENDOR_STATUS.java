package startwithco.startwithbackend.util;

public enum VENDOR_STATUS {
    WAITING_CHAT,     // 실시간 상담 대기
    CONFIRMED_WAIT,   // 구매 확정 대기 (개발 완료 알람 시)
    CONFIRMED,        // 구매 확정 (개발 완료 알람 1~2주 후)
    CANCELED,         // 결제 취소
    SETTLEMENT_WAIT,  // 정산 대기 (개발 완료 알람 시)
    SETTLED,          // 정산 완료 (개발 완료 알람 1-2주 후)
    REFUND_REQUESTED  // 환불 요청
}
