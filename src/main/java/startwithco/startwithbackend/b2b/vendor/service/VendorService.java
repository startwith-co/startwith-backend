package startwithco.startwithbackend.b2b.vendor.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import startwithco.startwithbackend.b2b.consumer.controller.request.ConsumerRequest;
import startwithco.startwithbackend.b2b.consumer.controller.response.ConsumerResponse;
import startwithco.startwithbackend.b2b.consumer.domain.ConsumerEntity;
import startwithco.startwithbackend.b2b.vendor.controller.request.VendorRequest;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;
import startwithco.startwithbackend.b2b.vendor.repository.VendorEntityRepository;
import startwithco.startwithbackend.common.service.CommonService;
import startwithco.startwithbackend.exception.*;
import startwithco.startwithbackend.exception.code.ExceptionCodeMapper;
import startwithco.startwithbackend.payment.payment.domain.PaymentEntity;
import startwithco.startwithbackend.payment.payment.repository.PaymentEntityRepository;
import startwithco.startwithbackend.payment.paymentEvent.repository.PaymentEventEntityRepository;
import startwithco.startwithbackend.payment.snapshot.entity.TossPaymentDailySnapshotEntity;
import startwithco.startwithbackend.payment.snapshot.repository.TossPaymentDailySnapshotEntityRepository;
import startwithco.startwithbackend.solution.solution.domain.SolutionEntity;
import startwithco.startwithbackend.solution.solution.repository.SolutionEntityRepository;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static startwithco.startwithbackend.b2b.vendor.controller.request.VendorRequest.*;
import static startwithco.startwithbackend.b2b.vendor.controller.response.VendorResponse.*;
import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.*;
import static startwithco.startwithbackend.exception.code.ExceptionCodeMapper.getCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorService {
    private final VendorEntityRepository vendorEntityRepository;
    private final SolutionEntityRepository solutionEntityRepository;
    private final PaymentEntityRepository paymentEntityRepository;
    private final TossPaymentDailySnapshotEntityRepository tossPaymentDailySnapshotEntityRepository;

    private final CommonService commonService;

    private final BCryptPasswordEncoder encoder;

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.accessTokenExpiration}")
    private long accessTokenExpiration;
    @Value("${jwt.refreshTokenExpiration}")
    private long refreshTokenExpiration;

    @Transactional(readOnly = true)
    public List<GetVendorSolutionCategoryResponse> getVendorSolutionCategory(Long vendorSeq) {
        vendorEntityRepository.findByVendorSeq(vendorSeq)
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 벤더 기업입니다.",
                        getCode("존재하지 않는 벤더 기업입니다.", ExceptionType.NOT_FOUND)
                ));

        List<SolutionEntity> solutionEntities = solutionEntityRepository.findAllByVendorSeq(vendorSeq);

        List<GetVendorSolutionCategoryResponse> response = new ArrayList<>();
        for (SolutionEntity solutionEntity : solutionEntities) {
            response.add(new GetVendorSolutionCategoryResponse(solutionEntity.getCategory(), solutionEntity.getSolutionSeq()));
        }

        return response;
    }

    @Transactional
    public void saveVendor(SaveVendorRequest request, MultipartFile businessLicense) {
        vendorEntityRepository.findByEmail(request.email())
                .ifPresent(entity -> {
                    throw new ConflictException(
                            HttpStatus.CONFLICT.value(),
                            "중복된 이메일입니다.",
                            getCode("중복된 이메일입니다.", ExceptionType.CONFLICT)
                    );
                });

        try {
            String businessLicenseImage = commonService.uploadPDFFile(businessLicense);

            VendorEntity vendorEntity = VendorEntity.builder()
                    .vendorName(request.vendorName())
                    .managerName(request.managerName())
                    .phoneNumber(request.phoneNumber())
                    .email(request.email())
                    .encodedPassword(encoder.encode(request.password()))
                    .businessLicenseImage(businessLicenseImage)
                    .vendorUniqueType(UUID.randomUUID().toString())
                    .build();

            vendorEntityRepository.save(vendorEntity);

        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(
                    HttpStatus.CONFLICT.value(),
                    "동시성 저장은 불가능합니다.",
                    getCode("동시성 저장은 불가능합니다.", ExceptionType.CONFLICT)
            );
        }

    }

    public void validateEmail(String email) {

        vendorEntityRepository.findByEmail(email)
                .ifPresent(entity -> {
                    throw new ConflictException(
                            HttpStatus.CONFLICT.value(),
                            "이미 가입한 이메일 입니다.",
                            getCode("이미 가입한 이메일 입니다.", ExceptionType.CONFLICT)
                    );
                });
    }

    @Transactional
    public LoginVendorResponse login(LoginVendorRequest request) {

        // 이메일 검증
        VendorEntity vendorEntity = validateEmail(request);

        // 비밀번호 검증
        validatePassword(request, vendorEntity);

        // Access 토큰 생성
        Long consumerSeq = vendorEntity.getVendorSeq();

        String accessToken = generateToken(accessTokenExpiration, consumerSeq);

        // Refresh 토큰 생성
        String refreshToken = generateToken(refreshTokenExpiration, consumerSeq);

        // Refresh 토큰 저장
        saveRefreshToken(consumerSeq, refreshToken);

        return new LoginVendorResponse(accessToken, refreshToken, vendorEntity.getVendorSeq());
    }

    private VendorEntity validateEmail(VendorRequest.LoginVendorRequest request) {
        return vendorEntityRepository.findByEmail(request.email())
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 이메일 입니다.",
                        getCode("존재하지 않는 이메일 입니다.", ExceptionType.NOT_FOUND
                        )));
    }

    private void validatePassword(LoginVendorRequest request, VendorEntity vendorEntity) {
        if (!encoder.matches(request.password(), vendorEntity.getEncodedPassword())) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "비밀번호가 일치하지 않습니다.",
                    getCode("비밀번호가 일치하지 않습니다.", ExceptionType.BAD_REQUEST)
            );
        }
    }

    private String generateToken(long tokenExpiration, Long consumerSeq) {
        Date now = new Date();
        Date refreshExpiryDate = new Date(now.getTime() + tokenExpiration);
        return Jwts.builder()
                .setSubject(String.valueOf(consumerSeq))
                .setIssuedAt(now)
                .setExpiration(refreshExpiryDate)
                .claim("type", "password_reset")
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    private void saveRefreshToken(Long consumerSeq, String refreshToken) {

        try {
            redisTemplate.opsForValue().set(
                    String.valueOf(consumerSeq),
                    refreshToken,
                    refreshTokenExpiration,
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

    public GetVendorInfo getVendorInfo(Long vendorSeq) {

        VendorEntity vendorEntity = vendorEntityRepository.findByVendorSeq(vendorSeq)
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 벤더 기업입니다.",
                        getCode("존재하지 않는 벤더 기업입니다.", ExceptionType.NOT_FOUND)
                ));

        return GetVendorInfo.fromEntity(vendorEntity);
    }

    @Transactional
    public void updateVendor(UpdateVendorInfoRequest request, MultipartFile vendorBannerImageUrl) {
        VendorEntity vendorEntity = vendorEntityRepository.findByVendorSeq(request.vendorSeq())
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 벤더 기업입니다.",
                        getCode("존재하지 않는 벤더 기업입니다.", ExceptionType.NOT_FOUND)
                ));

        String uploadJPGFile = commonService.uploadJPGFile(vendorBannerImageUrl);

        vendorEntity.update(
                request.vendorName(),
                request.managerName(),
                request.phoneNumber(),
                request.email(),
                request.audit(),
                request.accountNumber(),
                request.bank(),
                request.vendorExplanation(),
                uploadJPGFile,
                request.weekdayAvailable(),
                request.weekdayStartTime(),
                request.weekdayEndTime(),
                request.weekendAvailable(),
                request.weekendStartTime(),
                request.weekendEndTime(),
                request.holidayAvailable(),
                request.holidayStartTime(),
                request.holidayEndTime(),
                request.orderCount(),
                request.clientCount()
        );

        vendorEntityRepository.save(vendorEntity);
    }

    @Transactional(readOnly = true)
    public GetVendorDashboardResponse getVendorDashboard(Long vendorSeq) {
        VendorEntity vendorEntity = vendorEntityRepository.findByVendorSeq(vendorSeq)
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 벤더 기업입니다.",
                        getCode("존재하지 않는 벤더 기업입니다.", ExceptionType.NOT_FOUND)
                ));

        return new GetVendorDashboardResponse(
                vendorEntity.getVendorSeq(),
                paymentEntityRepository.countDONEStatusByVendorSeq(vendorEntity.getVendorSeq()),
                paymentEntityRepository.countDONEStatusByVendorSeq(vendorEntity.getVendorSeq()),
                paymentEntityRepository.countSETTLEDStatusByVendorSeq(vendorEntity.getVendorSeq())
        );
    }

    @Transactional(readOnly = true)
    public List<Object> getVendorDashboardList(Long vendorSeq, String paymentStatus, int start, int end) {
        vendorEntityRepository.findByVendorSeq(vendorSeq)
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 벤더 기업입니다.",
                        getCode("존재하지 않는 벤더 기업입니다.", ExceptionType.NOT_FOUND)
                ));

        List<PaymentEntity> paymentEntities
                = paymentEntityRepository.findAllByVendorSeqAndPaymentStatus(vendorSeq, paymentStatus, start, end);

        List<Object> response = new ArrayList<>();
        for (PaymentEntity paymentEntity : paymentEntities) {
            TossPaymentDailySnapshotEntity tossPaymentDailySnapshotEntity
                    = tossPaymentDailySnapshotEntityRepository.findByOrderId(paymentEntity.getOrderId()).orElse(null);
            VendorEntity vendorEntity = paymentEntity.getPaymentEventEntity().getVendorEntity();
            SolutionEntity solutionEntity = paymentEntity.getPaymentEventEntity().getSolutionEntity();
            ConsumerEntity consumerEntity = paymentEntity.getPaymentEventEntity().getConsumerEntity();

            if (tossPaymentDailySnapshotEntity == null) {
                GetVendorDashboardDONEListResponse doneListResponse = new GetVendorDashboardDONEListResponse(
                        vendorEntity.getVendorSeq(),
                        paymentEntity.getPaymentStatus(),
                        solutionEntity.getSolutionSeq(),
                        solutionEntity.getSolutionName(),
                        paymentEntity.getAmount(),
                        paymentEntity.getAutoConfirmScheduledAt(),
                        paymentEntity.getAutoConfirmScheduledAt(),
                        consumerEntity.getConsumerSeq(),
                        consumerEntity.getConsumerName()
                );

                response.add(doneListResponse);
            } else {
                GetVendorDashboardSETTELEDListResponse settledListResponse = new GetVendorDashboardSETTELEDListResponse(
                        vendorEntity.getVendorSeq(),
                        paymentEntity.getPaymentStatus(),
                        solutionEntity.getSolutionSeq(),
                        solutionEntity.getSolutionName(),
                        paymentEntity.getAmount(),
                        paymentEntity.getAutoConfirmScheduledAt(),
                        tossPaymentDailySnapshotEntity.getSettlementAmount(),
                        consumerEntity.getConsumerSeq(),
                        consumerEntity.getConsumerName()
                );

                response.add(settledListResponse);
            }
        }

        return response;
    }

    public ResetLinkResponse resetLink(ResetLinkRequest request) {

        // 이메일 검증
        VendorEntity vendorEntity = vendorEntityRepository.findByEmail(request.email())
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 이메일 입니다.",
                        getCode("존재하지 않는 이메일 입니다.", ExceptionType.NOT_FOUND
                        )));

        // Vendor Name 검증
        if (!request.vendorName().equals(vendorEntity.getVendorName())) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Vendor Name이 일치하지 않습니다.",
                    getCode("Vendor Name이 일치하지 않습니다.", ExceptionType.BAD_REQUEST)
            );
        }

        // 토큰 생성
        String token = generateToken(1_800_000L, vendorEntity.getVendorSeq());

        String resetLink = "http://localhost:3000/forget/reset?token=" + token;

        commonService.sendResetLink(vendorEntity.getEmail(), resetLink);

        return new ResetLinkResponse(token, resetLink, vendorEntity.getVendorSeq());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request, String token) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long vendorSeq = Long.parseLong(authentication.getName());

        String blackToken;
        try {
            // 블랙리스트 조회
            blackToken = redisTemplate.opsForValue().get(token);
        } catch (Exception e) {
            throw new ServerException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Redis 서버 오류가 발생했습니다.",
                    getCode("Redis 서버 오류가 발생했습니다.", ExceptionType.SERVER)
            );
        }

        if (blackToken != null) {
            throw new UnauthorizedException(
                    HttpStatus.UNAUTHORIZED.value(),
                    "이미 사용한 JWT 입니다.",
                    getCode("이미 사용한 JWT 입니다.", ExceptionType.UNAUTHORIZED)
            );
        }

        // 이메일 검증
        VendorEntity vendorEntity = vendorEntityRepository.findByEmail(request.email())
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 이메일 입니다.",
                        getCode("존재하지 않는 이메일 입니다.", ExceptionType.NOT_FOUND
                        )));

        // 토큰 seq 검증
        if (!Objects.equals(vendorEntity.getVendorSeq(), vendorSeq)) {
            throw new UnauthorizedException(
                    HttpStatus.UNAUTHORIZED.value(),
                    "잘못된 JWT 입니다.",
                    getCode("잘못된 JWT 입니다.", ExceptionCodeMapper.ExceptionType.UNAUTHORIZED)
            );
        }

        // 비밀번호 검증
        if (!encoder.matches(request.password(), vendorEntity.getEncodedPassword())) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "비밀번호가 일치하지 않습니다.",
                    getCode("비밀번호가 일치하지 않습니다.", ExceptionType.BAD_REQUEST)
            );
        }

        // 비밀번호 수정
        vendorEntity.updatePassword(encoder.encode(request.newPassword()));

        vendorEntityRepository.save(vendorEntity);

        // 한번 사용한 토큰은 블랙리스트
        vendorEntityRepository.saveBlackToken(token);

    }

}
