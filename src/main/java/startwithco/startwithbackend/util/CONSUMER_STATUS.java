package startwithco.startwithbackend.util;

public enum CONSUMER_STATUS {
    DEVELOPING,       // 개발 진행 중 (결제 완료 시)
    DEVELOPED,        // 개발 완료 (개발 완료 알람 시)
    CONFIRMED,        // 구매 확정 (개발 완료 알람 1-2주 후)
    CANCELED
}
