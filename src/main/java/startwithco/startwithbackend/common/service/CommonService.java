package startwithco.startwithbackend.common.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import reactor.core.publisher.Mono;
import startwithco.startwithbackend.exception.BadRequestException;
import startwithco.startwithbackend.exception.NotFoundException;
import startwithco.startwithbackend.exception.ServerException;
import startwithco.startwithbackend.payment.payment.domain.PaymentEntity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.util.UUID.randomUUID;
import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.*;
import static startwithco.startwithbackend.b2b.vendor.controller.request.VendorRequest.*;

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

    @Qualifier("frontWebClient")
    private final WebClient frontWebClient;

    private final TemplateEngine templateEngine;

    private final JavaMailSender javaMailSender;

    private final RedisTemplate<String, String> redisTemplate;

    private final ObjectMapper objectMapper;

    public String uploadJPGFile(MultipartFile multipartFile) {
        try {
            String fileName = randomUUID().toString() + ".jpg";
            InputStream inputStream = multipartFile.getInputStream();

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(multipartFile.getSize());
            metadata.setContentType(multipartFile.getContentType());

            s3client.putObject(new PutObjectRequest(bucketName, fileName, inputStream, metadata));
            return s3client.getUrl(bucketName, fileName).toString();
        } catch (IOException e) {
            throw new ServerException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "S3 UPLOAD 실패",
                    getCode("S3 UPLOAD 실패", ExceptionType.SERVER)
            );
        }
    }

    public List<String> uploadJPGFileList(List<MultipartFile> multipartFiles) {
        try {
            String fileName = randomUUID().toString() + ".jpg";

            List<String> imageUrls = new ArrayList<>();

            for (MultipartFile multipartFile : multipartFiles) {
                InputStream inputStream = multipartFile.getInputStream();

                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(multipartFile.getSize());
                metadata.setContentType(multipartFile.getContentType());

                s3client.putObject(new PutObjectRequest(bucketName, fileName, inputStream, metadata));

                imageUrls.add(s3client.getUrl(bucketName, fileName).toString());
            }

            return imageUrls;
        } catch (IOException e) {
            throw new ServerException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "S3 UPLOAD 실패",
                    getCode("S3 UPLOAD 실패", ExceptionType.SERVER)
            );
        }
    }

    public String uploadPDFFile(MultipartFile pdfFile) {
        try {
            String fileName = randomUUID().toString() + ".pdf";
            InputStream inputStream = pdfFile.getInputStream();

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(pdfFile.getSize());
            metadata.setContentType("application/pdf");

            s3client.putObject(new PutObjectRequest(bucketName, fileName, inputStream, metadata));
            return s3client.getUrl(bucketName, fileName).toString();
        } catch (IOException e) {
            throw new ServerException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "S3 UPLOAD 실패",
                    getCode("S3 UPLOAD 실패", ExceptionType.SERVER)
            );
        }
    }

    public Mono<JsonNode> executeTossPaymentApproval(String paymentKey, String orderId, Long amount) {
        String encodedSecretKey = Base64.getEncoder()
                .encodeToString((tossPaymentSecretKey + ":").getBytes(StandardCharsets.UTF_8));

        return tossPaymentWebClient.post()
                .uri("/confirm")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedSecretKey)
                .header("Idempotency-Key", paymentKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "paymentKey", paymentKey,
                        "orderId", orderId,
                        "amount", amount
                ))
                .retrieve()
                .bodyToMono(String.class)
                .<JsonNode>handle((responseBody, sink) -> {
                    try {
                        sink.next(objectMapper.readTree(responseBody));
                    } catch (Exception e) {
                        sink.error(new ServerException(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "결제 응답 파싱 중 오류가 발생했습니다.",
                                getCode("결제 응답 파싱 중 오류가 발생했습니다.", ExceptionType.SERVER)
                        ));
                    }
                })
                .doOnSuccess(json -> log.info("✅ 결제 승인 성공"))
                .doOnError(WebClientResponseException.class, err -> {
                    String responseBody = err.getResponseBodyAsString();
                    String errorMessage = "TOSS 결제 승인 실패";

                    try {
                        JsonNode errorJson = objectMapper.readTree(responseBody);
                        if (errorJson.has("message")) {
                            errorMessage = errorJson.get("message").asText();
                        }
                    } catch (Exception parseError) {
                        log.warn("⚠️ Toss 오류 응답 파싱 실패: {}", responseBody);
                    }

                    throw new ServerException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            errorMessage,
                            getCode(errorMessage, ExceptionType.SERVER)
                    );
                })
                .onErrorResume(err -> {
                    return Mono.error(new ServerException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            err.getMessage(),
                            getCode(err.getMessage(), ExceptionType.SERVER)
                    ));
                });
    }

    public Mono<JsonNode> cancelTossPaymentApproval(String paymentKey, String cancelReason,
                                                    String bankCode, String accountNumber, String holderName) {
        String encodedSecretKey = Base64.getEncoder()
                .encodeToString((tossPaymentSecretKey + ":").getBytes(StandardCharsets.UTF_8));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("cancelReason", cancelReason);

        if (bankCode != null && accountNumber != null && holderName != null) {
            Map<String, String> refundReceiveAccount = new HashMap<>();
            refundReceiveAccount.put("bank", bankCode);
            refundReceiveAccount.put("accountNumber", accountNumber);
            refundReceiveAccount.put("holderName", holderName);
            requestBody.put("refundReceiveAccount", refundReceiveAccount);
        }

        return tossPaymentWebClient.post()
                .uri("/{paymentKey}/cancel", paymentKey)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedSecretKey)
                .header("Idempotency-Key", paymentKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .<JsonNode>handle((responseBody, sink) -> {
                    try {
                        sink.next(objectMapper.readTree(responseBody));
                    } catch (Exception e) {
                        sink.error(new ServerException(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "결제 응답 파싱 중 오류가 발생했습니다.",
                                getCode("결제 응답 파싱 중 오류가 발생했습니다.", ExceptionType.SERVER)
                        ));
                    }
                })
                .doOnSuccess(json -> log.info("✅ 결제 취소 성공: {}", json))
                .doOnError(WebClientResponseException.class, err -> {
                    throw new ServerException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            err.getMessage(),
                            getCode(err.getMessage(), ExceptionType.SERVER)
                    );
                })
                .onErrorResume(err -> {
                    return Mono.error(new ServerException(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            err.getMessage(),
                            getCode(err.getMessage(), ExceptionType.SERVER)
                    ));
                });
    }

    public void notifyFrontOfVirtualAccountStatus(PaymentEntity paymentEntity) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("paymentSeq", paymentEntity.getPaymentSeq());
        payload.put("orderId", paymentEntity.getOrderId());
        payload.put("paymentKey", paymentEntity.getPaymentKey());
        payload.put("amount", paymentEntity.getAmount());
        payload.put("paymentStatus", paymentEntity.getPaymentStatus().name());
        payload.put("method", paymentEntity.getMethod() != null ? paymentEntity.getMethod().name() : null);
        payload.put("secret", paymentEntity.getSecret());
        payload.put("paymentCompletedAt", paymentEntity.getPaymentCompletedAt() != null ? paymentEntity.getPaymentCompletedAt().toString() : null);
        payload.put("autoConfirmScheduledAt", paymentEntity.getAutoConfirmScheduledAt() != null ? paymentEntity.getAutoConfirmScheduledAt().toString() : null);
        payload.put("dueDate", paymentEntity.getDueDate() != null ? paymentEntity.getDueDate().toString() : null);

        frontWebClient.post()
                .uri("/api/deposit-webhook")
                .bodyValue(payload)
                .retrieve()
                .toBodilessEntity()
                .doOnError(err -> log.error("❌ 프론트 전달 실패: error={}", err.getMessage()))
                .subscribe();
    }

    public void sendResetLink(String email, String link) {

        String subject = "Solu 비밀번호 재설정";

        Context context = new Context();
        context.setVariable("link", link);

        String htmlContent = templateEngine.process("reset-link-template", context);

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            // 로고 이미지 첨부 (cid를 사용하여 inline 삽입)
            // 로고 생기면
//            helper.addInline("logoImage", new ClassPathResource("static/images/logo.png"));

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new ServerException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "이메일 전송 중 오류가 발생했습니다.",
                    getCode("이메일 전송 중 오류가 발생했습니다.", ExceptionType.SERVER)
            );
        }
    }

    public String sendAuthKey(String email) {

        Random random = new Random();
        String authKey = String.valueOf(random.nextInt(888888) + 111111);

        String subject = "Solu 이메일 인증번호";

        // Thymeleaf 컨텍스트 설정
        Context context = new Context();
        context.setVariable("authKey", authKey);

        // HTML 템플릿 렌더링
        String htmlContent = templateEngine.process("email-template", context);

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            // 로고 이미지 첨부 (cid를 사용하여 inline 삽입)
            // 로고 생기면
//            helper.addInline("logoImage", new ClassPathResource("static/images/logo.png"));

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new ServerException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "이메일 전송 중 오류가 발생했습니다.",
                    getCode("이메일 전송 중 오류가 발생했습니다.", ExceptionType.SERVER)
            );
        }

        return authKey;
    }

    public void sendVendorInfo(String email, String name) {

        String subject = "Solu 벤더 기업 입점 심사";

        // Thymeleaf 컨텍스트 설정
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("email", email);

        // HTML 템플릿 렌더링
        String htmlContent = templateEngine.process("vendor-verify", context);

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

            helper.setTo("startwith0325@gmail.com");
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            // 로고 이미지 첨부 (cid를 사용하여 inline 삽입)
            // 로고 생기면
//            helper.addInline("logoImage", new ClassPathResource("static/images/logo.png"));

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new ServerException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "이메일 전송 중 오류가 발생했습니다.",
                    getCode("이메일 전송 중 오류가 발생했습니다.", ExceptionType.SERVER)
            );
        }
    }

    public void saveAuthKey(String email, String authKey, String type) {
        try {
            redisTemplate.opsForValue().set(
                    email + type,
                    authKey,
                    300000,
                    TimeUnit.MILLISECONDS
            );
        } catch (Exception e) {
            throw new ServerException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Redis 서버 오류가 발생했습니다.",
                    getCode("Redis 서버 오류가 발생했습니다.", ExceptionType.SERVER)
            );
        }

    }

    public void verifyCode(VerifyCodeRequest request, String type) {

        String code = redisTemplate.opsForValue().get(request.email() + type);

        if (code == null) {
            throw new NotFoundException(
                    HttpStatus.NOT_FOUND.value(),
                    "존재하지 않는 코드입니다",
                    getCode("존재하지 않는 코드입니다", ExceptionType.NOT_FOUND)
            );
        }

        if (!code.equals(request.code())) {
            throw new BadRequestException(
                    HttpStatus.NOT_FOUND.value(),
                    "인증코드가 일치하지 않습니다.",
                    getCode("인증코드가 일치하지 않습니다.", ExceptionType.BAD_REQUEST)
            );
        }

        redisTemplate.delete(request.email());

    }
}
