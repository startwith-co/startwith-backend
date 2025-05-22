package startwithco.startwithbackend.util;

public enum STATUS {
    READY,        // 결제 준비
    EXECUTED,     // 결제 승인 요청됨
    COMPLETED,    // 결제 완료
    CANCELED,     // 사용자에 의해 취소
    FAILURE
}
