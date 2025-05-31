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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Random;
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
                .header("Idempotency-Key", orderId)
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
                .doOnSuccess(json -> log.info("✅ 결제 승인 성공: {}", json))
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

    public String sendAuthKey(String email) {

        Random random = new Random();
        String authKey =  String.valueOf(random.nextInt(888888) + 111111);

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

    public void saveAuthKey(String email, String authKey, String type) {
        try {
            redisTemplate.opsForValue().set(
                    email+type,
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

    public void verifyCode(VerifyCodeRequest request,String type) {

        String code = redisTemplate.opsForValue().get(request.email()+type);

        if(code == null) {
            throw new NotFoundException(
                    HttpStatus.NOT_FOUND.value(),
                    "존재하지 않는 코드입니다",
                    getCode("존재하지 않는 코드입니다", ExceptionType.NOT_FOUND)
            );
        }

        if(!code.equals(request.code())) {
            throw new BadRequestException(
                    HttpStatus.NOT_FOUND.value(),
                    "인증코드가 일치하지 않습니다.",
                    getCode("인증코드가 일치하지 않습니다.", ExceptionType.BAD_REQUEST)
            );
        }

        redisTemplate.delete(request.email());

    }
}
