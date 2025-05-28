package startwithco.startwithbackend.common.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import startwithco.startwithbackend.exception.ServerException;
import startwithco.startwithbackend.exception.code.ExceptionCodeMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static java.util.UUID.randomUUID;
import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommonService {
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    private final AmazonS3 s3client;

    @Value("${toss.payment.secret-key}")
    private String tossPaymentSecretKey;

    @Qualifier("tossPaymentWebClient")
    private final WebClient tossPaymentWebClient;

    public String uploadJPGFile(MultipartFile multipartFile) throws IOException {
        String fileName = randomUUID().toString() + ".jpg";
        InputStream inputStream = multipartFile.getInputStream();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        s3client.putObject(new PutObjectRequest(bucketName, fileName, inputStream, metadata));
        return s3client.getUrl(bucketName, fileName).toString();
    }

    public String uploadPDFFile(MultipartFile pdfFile) throws IOException {
        String fileName = randomUUID().toString() + ".pdf";
        InputStream inputStream = pdfFile.getInputStream();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(pdfFile.getSize());
        metadata.setContentType("application/pdf");

        s3client.putObject(new PutObjectRequest(bucketName, fileName, inputStream, metadata));
        return s3client.getUrl(bucketName, fileName).toString();
    }

    public Mono<String> executeTossPaymentApproval(String paymentKey, String orderId, Long amount) {
        String encodedSecretKey = Base64.getEncoder()
                .encodeToString((tossPaymentSecretKey + ":").getBytes(StandardCharsets.UTF_8));

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
                    return Mono.error(new ServerException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "내부 서버 오류가 발생했습니다.",
                            getCode("내부 서버 오류가 발생했습니다.", ExceptionType.SERVER)
                    ));
                });
    }
}
