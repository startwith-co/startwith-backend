package startwithco.startwithbackend.payment.payment.util;

public enum PAYMENT_STATUS {
    DONE,                       // 승인 성공
    CANCELED,                   // 결제 취소
    PARTIAL_CANCELED,           // 결제 부분 취소
    EXPIRED,                    // 유효 시간 만료
    ABORTED,                    // 승인 실패
    WAITING_FOR_DEPOSIT,        // 입금 대기
    SETTLED                     // 정산 완료
    ;
}