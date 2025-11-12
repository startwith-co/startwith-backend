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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import startwithco.startwithbackend.b2b.client.domain.ClientEntity;
import startwithco.startwithbackend.b2b.client.repository.ClientEntityRepository;
import startwithco.startwithbackend.admin.settlement.dto.VendorDto;
import startwithco.startwithbackend.b2b.consumer.domain.ConsumerEntity;
import startwithco.startwithbackend.b2b.consumer.repository.ConsumerRepository;
import startwithco.startwithbackend.b2b.stat.domain.StatEntity;
import startwithco.startwithbackend.b2b.stat.repository.StatEntityRepository;
import startwithco.startwithbackend.b2b.vendor.controller.request.VendorRequest;
import startwithco.startwithbackend.b2b.vendor.domain.VendorEntity;
import startwithco.startwithbackend.b2b.vendor.repository.VendorEntityRepository;
import startwithco.startwithbackend.common.service.CommonService;
import startwithco.startwithbackend.exception.*;
import startwithco.startwithbackend.exception.code.ExceptionCodeMapper;
import startwithco.startwithbackend.payment.payment.domain.PaymentEntity;
import startwithco.startwithbackend.payment.payment.repository.PaymentEntityRepository;
import startwithco.startwithbackend.payment.snapshot.entity.TossPaymentDailySnapshotEntity;
import startwithco.startwithbackend.payment.snapshot.repository.TossPaymentDailySnapshotEntityRepository;
import startwithco.startwithbackend.solution.solution.domain.SolutionEntity;
import startwithco.startwithbackend.solution.solution.repository.SolutionEntityRepository;

import java.util.*;
import java.util.stream.Collectors;

import static startwithco.startwithbackend.b2b.client.controller.response.ClientResponse.*;
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
    private final StatEntityRepository statRepository;
    private final ClientEntityRepository clientRepository;
    private final ConsumerRepository consumerRepository;

    private final CommonService commonService;

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.accessTokenExpiration}")
    private long accessTokenExpiration;
    @Value("${jwt.refreshTokenExpiration}")
    private long refreshTokenExpiration;
    @Value("${app.frontend.reset-link}")
    private String resetLink;

    @Transactional(readOnly = true)
    public List<GetVendorSolutionCategoryResponse> getVendorSolutionCategory(Long vendorSeq) {
        vendorEntityRepository.findByVendorSeq(vendorSeq)
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 벤더 기업입니다.",
                        getCode("존재하지 않는 벤더 기업입니다.", ExceptionType.NOT_FOUND)
                ));

        List<SolutionEntity> solutionEntities = solutionEntityRepository.findAllByVendorSeq(vendorSeq);

        return solutionEntities.stream()
                .map(solutionEntity -> new GetVendorSolutionCategoryResponse(
                        solutionEntity.getCategory(),
                        solutionEntity.getSolutionSeq()
                ))
                .toList();
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
                    .encodedPassword(commonService.encodePassword(request.password()))
                    .businessLicenseImage(businessLicenseImage)
                    .vendorUniqueType(UUID.randomUUID().toString())
                    .build();

            vendorEntityRepository.save(vendorEntity);

            commonService.sendVendorInfo(vendorEntity.getEmail(), vendorEntity.getVendorName());

        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(
                    HttpStatus.CONFLICT.value(),
                    e.getMessage(),
                    getCode(e.getMessage(), ExceptionType.CONFLICT)
            );
        } catch (Exception e) {
            throw new ServerException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    e.getMessage(),
                    getCode(e.getMessage(), ExceptionType.SERVER)
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
        commonService.validatePassword(request.password(), vendorEntity.getEncodedPassword());

        // Access 토큰 생성
        Long consumerSeq = vendorEntity.getVendorSeq();

        String accessToken = generateToken(accessTokenExpiration, consumerSeq);

        // Refresh 토큰 생성
        String refreshToken = generateToken(refreshTokenExpiration, consumerSeq);

        // Refresh 토큰 저장
        commonService.saveRefreshToken(consumerSeq, refreshToken, refreshTokenExpiration);

        return new LoginVendorResponse(accessToken, refreshToken, vendorEntity.getVendorSeq(), vendorEntity.getVendorUniqueType(), vendorEntity.getVendorName());
    }

    private VendorEntity validateEmail(VendorRequest.LoginVendorRequest request) {
        return vendorEntityRepository.findByEmail(request.email())
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 이메일 입니다.",
                        getCode("존재하지 않는 이메일 입니다.", ExceptionType.NOT_FOUND
                        )));
    }

    private String generateToken(long tokenExpiration, Long vendorSeq) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + tokenExpiration);
        return Jwts.builder()
                .setSubject(String.valueOf(vendorSeq))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("role", "VENDOR")
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }


    @Transactional(readOnly = true)
    public GetVendorInfo getVendorInfo(Long vendorSeq) {

        VendorEntity vendorEntity = vendorEntityRepository.findByVendorSeq(vendorSeq)
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 벤더 기업입니다.",
                        getCode("존재하지 않는 벤더 기업입니다.", ExceptionType.NOT_FOUND)
                ));


        List<StatResponse> statResponses = statRepository.findAllByVendorCustom(vendorEntity);

        List<GetAllClientResponse> clientResponses = clientRepository.findAllByVendorSeqCustom(vendorSeq);

        return GetVendorInfo.fromEntity(vendorEntity, statResponses, clientResponses);
    }

    @Transactional
    public void updateVendor(UpdateVendorInfoRequest request, MultipartFile vendorBannerImageUrl, List<MultipartFile> clientInfos, MultipartFile profileImage) {
        VendorEntity vendorEntity = vendorEntityRepository.findByVendorSeq(request.vendorSeq())
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 벤더 기업입니다.",
                        getCode("존재하지 않는 벤더 기업입니다.", ExceptionType.NOT_FOUND)
                ));

        String vendorBannerImage = (vendorBannerImageUrl != null && !vendorBannerImageUrl.isEmpty()) ? commonService.uploadJPGFile(vendorBannerImageUrl) : null;
        String vendorProfileImage = (profileImage != null && !profileImage.isEmpty()) ? commonService.uploadJPGFile(profileImage) : null;

        vendorEntity.update(
                request.vendorName(),
                request.managerName(),
                request.phoneNumber(),
                request.email(),
                request.audit(),
                request.accountNumber(),
                request.bank(),
                request.vendorExplanation(),
                vendorBannerImage,
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
                request.clientCount(),
                vendorProfileImage
        );

        vendorEntityRepository.save(vendorEntity);

        if (request.stats() != null && !request.stats().isEmpty()) {
            statRepository.deleteAllByVendor(vendorEntity);

            List<StatEntity> statEntities = request.stats().stream()
                    .map(statInfo -> StatEntity.builder()
                            .label(statInfo.label())
                            .percentage(statInfo.percentage())
                            .statType(statInfo.statType())
                            .vendor(vendorEntity)
                            .build())
                    .collect(Collectors.toList());

            statRepository.saveAll(statEntities);
        }


        if (clientInfos != null && !clientInfos.isEmpty()) {
            clientRepository.deleteAllByVendor(vendorEntity);

            List<String> imageUrls = commonService.uploadJPGFileList(clientInfos);

            List<ClientEntity> clientEntities = imageUrls.stream()
                    .map(imageUrl -> ClientEntity.builder()
                            .logoImageUrl(imageUrl)
                            .vendorEntity(vendorEntity)
                            .build())
                    .collect(Collectors.toList());

            clientRepository.saveAll(clientEntities);
        }

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

        return paymentEntities.stream()
                .<Object>map(paymentEntity -> {
                    TossPaymentDailySnapshotEntity tossPaymentDailySnapshotEntity
                            = tossPaymentDailySnapshotEntityRepository.findByOrderId(paymentEntity.getOrderId()).orElse(null);
                    VendorEntity vendorEntity = paymentEntity.getPaymentEventEntity().getVendorEntity();
                    SolutionEntity solutionEntity = paymentEntity.getPaymentEventEntity().getSolutionEntity();
                    ConsumerEntity consumerEntity = paymentEntity.getPaymentEventEntity().getConsumerEntity();

                    if (tossPaymentDailySnapshotEntity == null) {
                        return new GetVendorDashboardDONEListResponse(
                                vendorEntity.getVendorSeq(),
                                paymentEntity.getPaymentStatus(),
                                solutionEntity.getSolutionSeq(),
                                solutionEntity.getSolutionName(),
                                paymentEntity.getAmount(),
                                paymentEntity.getDueDate(),
                                paymentEntity.getPaymentCompletedAt(),
                                consumerEntity.getConsumerSeq(),
                                consumerEntity.getConsumerName()
                        );
                    } else {
                        return new GetVendorDashboardSETTELEDListResponse(
                                vendorEntity.getVendorSeq(),
                                paymentEntity.getPaymentStatus(),
                                solutionEntity.getSolutionSeq(),
                                solutionEntity.getSolutionName(),
                                paymentEntity.getAmount(),
                                paymentEntity.getDueDate(),
                                tossPaymentDailySnapshotEntity.getSettlementAmount(),
                                consumerEntity.getConsumerSeq(),
                                consumerEntity.getConsumerName()
                        );
                    }
                })
                .toList();
    }

    public ResetLinkResponse resetLink(ResetLinkRequest request) {
        VendorEntity vendorEntity = vendorEntityRepository.findByEmail(request.email())
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 이메일 입니다.",
                        getCode("존재하지 않는 이메일 입니다.", ExceptionType.NOT_FOUND
                        )));

        if (!request.vendorName().equals(vendorEntity.getVendorName())) {
            throw new BadRequestException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Vendor Name이 일치하지 않습니다.",
                    getCode("Vendor Name이 일치하지 않습니다.", ExceptionType.BAD_REQUEST)
            );
        }

        String token = commonService.generatePasswordResetToken(1_800_000L, vendorEntity.getVendorSeq());

        String resetUrl = resetLink + "/forget/reset?user=vendor&token=ey";
        commonService.sendResetLink(vendorEntity.getEmail(), resetUrl);

        return new ResetLinkResponse(token, resetUrl, vendorEntity.getVendorSeq());
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
                    e.getMessage(),
                    getCode(e.getMessage(), ExceptionType.SERVER)
            );
        }

        if (blackToken != null) {
            throw new UnauthorizedException(
                    HttpStatus.UNAUTHORIZED.value(),
                    "이미 사용한 JWT 입니다.",
                    getCode("이미 사용한 JWT 입니다.", ExceptionType.UNAUTHORIZED)
            );
        }

        VendorEntity vendorEntity = vendorEntityRepository.findByVendorSeq(vendorSeq)
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 벤더 기업입니다.",
                        getCode("존재하지 않는 벤더 기업입니다.", ExceptionType.NOT_FOUND)
                ));

        // 토큰 seq 검증
        if (!Objects.equals(vendorEntity.getVendorSeq(), vendorSeq)) {
            throw new UnauthorizedException(
                    HttpStatus.UNAUTHORIZED.value(),
                    "잘못된 JWT 입니다.",
                    getCode("잘못된 JWT 입니다.", ExceptionCodeMapper.ExceptionType.UNAUTHORIZED)
            );
        }

        // 비밀번호 수정
        vendorEntity.updatePassword(commonService.encodePassword(request.newPassword()));

        vendorEntityRepository.save(vendorEntity);

        // 한번 사용한 토큰은 블랙리스트
        vendorEntityRepository.saveBlackToken(token);

    }

    @Transactional(readOnly = true)
    public List<VendorDto> getAllVendorEntity(int start, int end) {
        return vendorEntityRepository.getAllVendorEntity(start, end).stream()
                .map(vendor -> VendorDto.builder()
                        .vendorName(vendor.getVendorName())
                        .managerName(vendor.getManagerName())
                        .phoneNumber(vendor.getPhoneNumber())
                        .email(vendor.getEmail())
                        .audit(vendor.isAudit())
                        .registerCompletedAt(vendor.getCreatedAt()) // BaseTimeEntity 상속 필드
                        .build())
                .toList();
    }

    @Transactional
    public void approveVendorEntity(String email) {
        VendorEntity vendorEntity = vendorEntityRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 벤더 기업입니다.",
                        getCode("존재하지 않는 벤더 기업입니다.", ExceptionType.NOT_FOUND)
                ));

        vendorEntity.updateAudit(true);
        vendorEntityRepository.save(vendorEntity);
    }

    @Transactional
    public void cancelVendorEntity(String email) {
        VendorEntity vendorEntity = vendorEntityRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 벤더 기업입니다.",
                        getCode("존재하지 않는 벤더 기업입니다.", ExceptionType.NOT_FOUND)
                ));

        vendorEntity.updateAudit(false);
        vendorEntityRepository.save(vendorEntity);
    }

    @Transactional(readOnly = true)
    public List<GetVendorSolutionEntitiesResponse> getVendorSolutionEntities(Long vendorSeq) {
        vendorEntityRepository.findByVendorSeq(vendorSeq)
                .orElseThrow(() -> new NotFoundException(
                        HttpStatus.NOT_FOUND.value(),
                        "존재하지 않는 벤더 기업입니다.",
                        getCode("존재하지 않는 벤더 기업입니다.", ExceptionType.NOT_FOUND)
                ));

        return solutionEntityRepository.findAllByVendorSeq(vendorSeq).stream()
                .map(s -> new GetVendorSolutionEntitiesResponse(
                        s.getSolutionSeq(),
                        s.getSolutionName(),
                        s.getCategory(),
                        s.getAmount()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean conflictVendorConsumerEntity(String email, String type) {
        if (type == null || email == null) return false;

        if (type.equalsIgnoreCase("VENDOR")) {
            return vendorEntityRepository.findByEmail(email).isPresent();
        }

        if (type.equalsIgnoreCase("CONSUMER")) {
            return consumerRepository.findByEmail(email).isPresent();
        }

        return false;
    }
}
