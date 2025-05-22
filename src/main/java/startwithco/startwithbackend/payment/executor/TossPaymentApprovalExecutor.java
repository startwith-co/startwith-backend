package startwithco.startwithbackend.payment.executor;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import startwithco.startwithbackend.exception.server.ServerErrorResult;
import startwithco.startwithbackend.exception.server.ServerException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentApprovalExecutor {

    @Value("${toss.payment.secret-key}")
    private String secretKey;

    private final WebClient tossPaymentWebClient;

    public Mono<String> executeApproval(String paymentKey, String orderId, Long amount) {
        String encodedSecretKey = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        return tossPaymentWebClient.post()
                .uri("/confirm")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedSecretKey)
                .header("Idempotency-Key", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "paymentKey", paymentKey,
                        "orderId", orderId,
                        "amount", amount
                ))
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(res -> log.info("✅ 결제 승인 성공: {}", res))
                .doOnError(WebClientResponseException.class, err ->
                        log.error("❌ 결제 승인 실패 - 응답 에러: {}", err.getResponseBodyAsString()))
                .onErrorResume(Throwable.class, err -> {
                    log.error("❌ 결제 승인 실패: {}", err.getMessage());
                    return Mono.error(new ServerException(ServerErrorResult.INTERNAL_SERVER_EXCEPTION));
                });
    }
}