package startwithco.startwithbackend.payment.payment.util;

public enum PAYMENT_STATUS {
    IN_PROGRESS,   // 결제 승인 요청 중 (WebClient 요청 등 진행 상태)
    SUCCESS,       // 결제 승인 성공
    FAILURE,       // 결제 승인 실패 (PG사 응답 오류 또는 금액 불일치 등)
    CANCELLED,     // 결제 요청 취소
    REFUNDED       // 환불 처리
}