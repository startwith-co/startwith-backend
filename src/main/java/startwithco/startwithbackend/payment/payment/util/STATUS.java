package startwithco.startwithbackend.payment.payment.util;

public enum STATUS {
    APPROVAL_FAILED,  // 결제 승인 실패
    NOT_READY,        // 결제 완료 후 아직 정산일 전
    SETTLED,          // 판매자에게 실제 지급 완료
    FAILED            // 지급 실패 (정산 실패 대응 필요)
}
