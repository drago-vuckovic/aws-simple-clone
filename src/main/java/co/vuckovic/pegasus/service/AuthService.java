package co.vuckovic.pegasus.service;

import co.vuckovic.pegasus.model.dto.JwtUser;
import co.vuckovic.pegasus.model.dto.Tenant;
import co.vuckovic.pegasus.model.dto.User;
import co.vuckovic.pegasus.util.JwtUtil;
import co.vuckovic.pegasus.config.BucketProperties;
import co.vuckovic.pegasus.config.LambdaProperties;
import co.vuckovic.pegasus.model.enumeration.Role;
import co.vuckovic.pegasus.model.enumeration.SubscriptionType;
import co.vuckovic.pegasus.model.enumeration.TenantStatus;
import co.vuckovic.pegasus.model.exception.BucketException;
import co.vuckovic.pegasus.model.exception.ConflictException;
import co.vuckovic.pegasus.model.exception.EmailNotFoundException;
import co.vuckovic.pegasus.model.exception.NotFoundException;
import co.vuckovic.pegasus.model.request.ForgotPasswordRequest;
import co.vuckovic.pegasus.model.request.LoginRequest;
import co.vuckovic.pegasus.model.request.RecoveryPasswordRequest;
import co.vuckovic.pegasus.model.request.RefreshTokenRequest;
import co.vuckovic.pegasus.model.request.ResendEmailRequest;
import co.vuckovic.pegasus.model.request.SignUpRequest;
import co.vuckovic.pegasus.model.request.VerifyEmailRequest;
import co.vuckovic.pegasus.model.response.LoginResponse;
import co.vuckovic.pegasus.model.response.RefreshTokenResponse;
import co.vuckovic.pegasus.model.response.SignUpResponse;
import co.vuckovic.pegasus.repository.BucketEntityRepository;
import co.vuckovic.pegasus.repository.StatisticEntityRepository;
import co.vuckovic.pegasus.repository.SubscriptionPackageEntityRepository;
import co.vuckovic.pegasus.repository.TenantEntityRepository;
import co.vuckovic.pegasus.repository.UserEntityRepository;
import co.vuckovic.pegasus.repository.entity.BucketEntity;
import co.vuckovic.pegasus.repository.entity.StatisticEntity;
import co.vuckovic.pegasus.repository.entity.SubscriptionPackageEntity;
import co.vuckovic.pegasus.repository.entity.TenantEntity;
import co.vuckovic.pegasus.repository.entity.UserEntity;
import io.jsonwebtoken.Claims;
import java.sql.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.utility.RandomString;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

  private final FileService fileService;
  private final UserEntityRepository userEntityRepository;
  private final PasswordEncoder passwordEncoder;
  private final ModelMapper modelMapper;
  private final TenantEntityRepository tenantEntityRepository;
  private final MailService mailService;
  private final JwtUtil jwtUtil;
  private final AuthenticationManager authenticationManager;

  private final BucketEntityRepository bucketEntityRepository;
  private final BucketProperties bucketProperties;

  private final SubscriptionPackageEntityRepository subscriptionPackageEntityRepository;

  private final StatisticEntityRepository statisticEntityRepository;

  private final RestTemplate restTemplate;

  private final LambdaProperties lambdaProperties;


  public SignUpResponse signUp(SignUpRequest request) {
    if (userEntityRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new ConflictException("Email already exists.");
    }

    if (tenantEntityRepository.existsByCompanyOrSubdomain(
        request.getCompany(), request.getSubdomain())) {
      throw new ConflictException("Company or subdomain already exists.");
    }

    TenantEntity tenantEntity = modelMapper.map(request, TenantEntity.class);
    tenantEntity.setId(0);
    tenantEntity.setStatus(TenantStatus.UNVERIFIED);
    tenantEntity.setTimestamp(new Timestamp(System.currentTimeMillis()));
    SubscriptionPackageEntity subscriptionPackageEntity =
        subscriptionPackageEntityRepository
            .findBySubscriptionType(SubscriptionType.DEFAULT)
            .orElseThrow(() -> new NotFoundException("Subscription type not found"));
    tenantEntity.setSubscriptionPackageEntity(subscriptionPackageEntity);
    tenantEntity.setTotalNumOfLambdas(0);
    StatisticEntity statisticEntity =
        StatisticEntity.builder().id(0).numOfDownloads(0).numOfUploads(0).numOfFiles(0).build();
    statisticEntity = statisticEntityRepository.save(statisticEntity);

    tenantEntity = tenantEntityRepository.save(tenantEntity);

    UserEntity userEntity = modelMapper.map(request, UserEntity.class);
    userEntity.setId(0);
    userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
    userEntity.setRole(Role.OWNER);
    userEntity.setEnabled(false);
    userEntity.setTenantId(tenantEntity.getId());

    userEntity = setVerificationCodeAndSendConfirmationMail(userEntity, tenantEntity);

    BucketEntity bucketEntity = new BucketEntity();
    bucketEntity.setId(0);
    bucketEntity.setSize(bucketProperties.getInitialSize());
    bucketEntity.setCapacity(bucketProperties.getCapacity() * 1024);
    bucketEntity.setName(tenantEntity.getCompany());
    bucketEntity.setTenantId(tenantEntity.getId());
    bucketEntity.setStatisticEntity(statisticEntity);
    bucketEntityRepository.save(bucketEntity);

    if (fileService.createBucketFolder(tenantEntity.getCompany())) {
      return new SignUpResponse(userEntity.getId());
    } else {
      throw new BucketException("Bucket already exists!");
    }
  }

  public void forgottenPassword(ForgotPasswordRequest forgotPasswordRequest) {
    UserEntity userEntity =
        userEntityRepository
            .findByEmail(forgotPasswordRequest.getEmail())
            .orElseThrow(
                () -> new EmailNotFoundException("This email does not have Pegasus account."));
    String randomCode = RandomString.make(64);
    userEntity.setVerificationCode(randomCode);
    userEntityRepository.save(userEntity);

    User user = modelMapper.map(userEntity, User.class);
    mailService.sendRecoveryMail(user);
  }

  public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
    // get refresh token
    String refreshToken = request.getRefreshToken();
    Claims claims = jwtUtil.parseJwt(refreshToken);

    // get user from claims
    UserEntity user =
        userEntityRepository
            .findByEmail(claims.getSubject())
            .orElseThrow(
                () -> new EmailNotFoundException("Token refresh failed. Email not found."));

    // generate new JWT
    String jwt = jwtUtil.generateJwt(user);

    RefreshTokenResponse response = new RefreshTokenResponse();
    response.setJwtToken(jwt);
    return response;
  }

  public void verifyEmail(VerifyEmailRequest verifyEmailRequest) {
    UserEntity user =
        userEntityRepository
            .findByIdAndVerificationCode(
                verifyEmailRequest.getId(), verifyEmailRequest.getVerificationCode())
            .orElseThrow(() -> new NotFoundException("Verification code is not valid!"));
    if (Boolean.TRUE.equals(user.getEnabled())) {
      throw new ConflictException("User has already verified this account");
    }
    TenantEntity tenantEntity =
        tenantEntityRepository
            .findByIdAndCompany(user.getTenantId(), verifyEmailRequest.getCompany())
            .orElseThrow(() -> new NotFoundException("Verification failed. Tenant not found!"));
    user.setVerificationCode(null);
    user.setEnabled(true);
    userEntityRepository.save(user);
    tenantEntity.setStatus(TenantStatus.VERIFIED);
    tenantEntityRepository.save(tenantEntity);
    try {
      restTemplate.postForObject(
          String.format(
              "%s/create-total-execution-time/%s",
              lambdaProperties.getBaseUrl(), tenantEntity.getId()),
          null,
          Integer.class);

    } catch (HttpStatusCodeException e) {
      throw new NotFoundException("Can't create total execution time record");
    }
  }

  public void recoverPassword(RecoveryPasswordRequest recoveryPasswordRequest) {
    UserEntity userEntity =
        userEntityRepository
            .findByVerificationCode(recoveryPasswordRequest.getVerificationCode())
            .orElseThrow(() -> new NotFoundException("Verification code not valid!"));

    userEntity.setPassword(passwordEncoder.encode(recoveryPasswordRequest.getPassword()));
    userEntity.setVerificationCode(null);
    userEntityRepository.save(userEntity);
  }

  public LoginResponse login(LoginRequest loginRequest) {
    LoginResponse response;
    try {
      Authentication authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                  loginRequest.getEmail(), loginRequest.getPassword()));
      JwtUser jwtUser = (JwtUser) authentication.getPrincipal();
      UserEntity userEntity =
          userEntityRepository.findById(jwtUser.getId()).orElseThrow(NotFoundException::new);
      TenantEntity tenantEntity =
          tenantEntityRepository
              .findById(userEntity.getTenantId())
              .orElseThrow(NotFoundException::new);

      response = modelMapper.map(userEntity, LoginResponse.class);
      response.setToken(jwtUtil.generateJwt(userEntity));
      response.setRefreshToken(jwtUtil.generateRefresh(userEntity));
      response.setCompany(tenantEntity.getCompany());
      response.setBucketId(
          bucketEntityRepository
              .findBucketEntityByTenantId(tenantEntity.getId())
              .orElseThrow(() -> new NotFoundException("Bucket Not Found"))
              .getId());
      response.setSubscriptionType(
          tenantEntity.getSubscriptionPackageEntity().getSubscriptionType());
    } catch (Exception e) {
      log.error(String.format("An error occurred while performing action: %s", e.getMessage()));
      throw new NotFoundException("Email or password are not valid.");
    }
    return response;
  }

  public void resendEmail(ResendEmailRequest resendEmailRequest) {
    UserEntity userEntity =
        userEntityRepository
            .findById(resendEmailRequest.getId())
            .orElseThrow(() -> new NotFoundException("Mail can't be resent"));
    TenantEntity tenantEntity =
        tenantEntityRepository
            .findById(userEntity.getTenantId())
            .orElseThrow(() -> new NotFoundException("Mail can't be resent"));

    Boolean isEnabled = userEntity.getEnabled();
    if (userEntity.getRole().equals(Role.OWNER)
        && !Boolean.TRUE.equals(isEnabled)
        && !tenantEntity.getStatus().equals(TenantStatus.VERIFIED)) {
      tenantEntity.setTimestamp(new Timestamp(System.currentTimeMillis()));
      tenantEntityRepository.save(tenantEntity);

      setVerificationCodeAndSendConfirmationMail(userEntity, tenantEntity);
    } else {
      throw new NotFoundException("Mail is already verified");
    }
  }

  private UserEntity setVerificationCodeAndSendConfirmationMail(
      UserEntity userEntity, TenantEntity tenantEntity) {
    String randomCode = RandomString.make(64);
    userEntity.setVerificationCode(randomCode);
    userEntity = userEntityRepository.save(userEntity);

    mailService.sendConfirmationMail(
        modelMapper.map(userEntity, User.class), modelMapper.map(tenantEntity, Tenant.class));

    return userEntity;
  }
}
