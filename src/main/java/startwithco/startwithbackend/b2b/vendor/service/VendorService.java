package startwithco.startwithbackend.b2b.vendor.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import startwithco.startwithbackend.b2b.consumer.controller.request.ConsumerRequest;
import startwithco.startwithbackend.b2b.consumer.domain.ConsumerEntity;
import startwithco.startwithbackend.b2b.vendor.controller.request.VendorRequest;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;
import startwithco.startwithbackend.b2b.vendor.repository.VendorEntityRepository;
import startwithco.startwithbackend.common.service.CommonService;
import startwithco.startwithbackend.exception.BadRequestException;
import startwithco.startwithbackend.exception.ConflictException;
import startwithco.startwithbackend.exception.NotFoundException;
import startwithco.startwithbackend.exception.ServerException;
import startwithco.startwithbackend.payment.paymentEvent.repository.PaymentEventEntityRepository;
import startwithco.startwithbackend.solution.solution.domain.SolutionEntity;
import startwithco.startwithbackend.solution.solution.repository.SolutionEntityRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    private final PaymentEventEntityRepository paymentEventEntityRepository;
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

    @Transactional(readOnly = true)
    public GetVendorSettlementManagementStatusResponse getVendorSettlementManagementStatus(Long vendorSeq) {
        vendorEntityRepository.findByVendorSeq(vendorSeq)
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 벤더 기업입니다.",
                        getCode("존재하지 않는 벤더 기업입니다.", ExceptionType.NOT_FOUND)
                ));

        Long requested = paymentEventEntityRepository.countREQUESTEDPaymentEntityByVendorSeq(vendorSeq);
        Long confirmed = paymentEventEntityRepository.countCONFIRMEDPaymentEntityByVendorSeq(vendorSeq);
        Long settled = paymentEventEntityRepository.countSETTLEDPaymentEntityByVendorSeq(vendorSeq);

        return new GetVendorSettlementManagementStatusResponse(vendorSeq, requested, confirmed, settled);
    }

    @Transactional(readOnly = true)
    public List<GetVendorSettlementManagementProgressResponse> getVendorSettlementManagementProgress(Long vendorSeq, String paymentEventStatus, int start, int end) {
        vendorEntityRepository.findByVendorSeq(vendorSeq)
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 벤더 기업입니다.",
                        getCode("존재하지 않는 벤더 기업입니다.", ExceptionType.NOT_FOUND)
                ));

        return vendorEntityRepository.getVendorSettlementManagementProgressCustom(vendorSeq, paymentEventStatus, start, end);
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

        return new LoginVendorResponse(accessToken, refreshToken);
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
}
