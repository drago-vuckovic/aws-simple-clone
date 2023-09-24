package co.vuckovic.pegasus.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.vuckovic.pegasus.api.v1.controller.AuthController;
import co.vuckovic.pegasus.common.util.ContainerBaseTest.ContainerBaseTestInitializer;
import co.vuckovic.pegasus.config.LambdaProperties;
import co.vuckovic.pegasus.model.enumeration.Role;
import co.vuckovic.pegasus.model.enumeration.SubscriptionType;
import co.vuckovic.pegasus.model.enumeration.TenantStatus;
import co.vuckovic.pegasus.model.exception.ConflictException;
import co.vuckovic.pegasus.model.exception.EmailNotFoundException;
import co.vuckovic.pegasus.model.exception.NotFoundException;
import co.vuckovic.pegasus.model.request.ForgotPasswordRequest;
import co.vuckovic.pegasus.model.request.LoginRequest;
import co.vuckovic.pegasus.model.request.RecoveryPasswordRequest;
import co.vuckovic.pegasus.model.request.SignUpRequest;
import co.vuckovic.pegasus.model.request.VerifyEmailRequest;
import co.vuckovic.pegasus.model.response.LoginResponse;
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
import co.vuckovic.pegasus.service.AuthService;
import java.io.File;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {ContainerBaseTestInitializer.class})
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
@RunWith(MockitoJUnitRunner.class)
class AuthControllerIT {

  @Autowired
  private AuthController authController;

  @Autowired
  private LambdaProperties lambdaProperties;

  @Autowired
  private UserEntityRepository userEntityRepository;

  @Autowired
  private TenantEntityRepository tenantEntityRepository;

  @Autowired
  private BucketEntityRepository bucketEntityRepository;

  @Autowired
  private StatisticEntityRepository statisticEntityRepository;

  @Autowired
  private SubscriptionPackageEntityRepository subscriptionPackageEntityRepository;

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private AuthService authService;

  @BeforeEach
  private void cleanUpBeforeEach() {
    userEntityRepository.deleteAll();
    tenantEntityRepository.deleteAll();
    bucketEntityRepository.deleteAll();
    statisticEntityRepository.deleteAll();
    subscriptionPackageEntityRepository.deleteAll();
  }

  @AfterAll
  private void cleanUpAfter() {
    userEntityRepository.deleteAll();
    tenantEntityRepository.deleteAll();
    bucketEntityRepository.deleteAll();
    statisticEntityRepository.deleteAll();
    subscriptionPackageEntityRepository.deleteAll();
  }

  @Test
  void
  signUp_withValidData_shouldCreateUnverifiedTenantAndDisabledUserWithRoleOwnerAndReturnInsertedUserId() {
    String firstname = "Test";
    String lastname = "Test";
    String email = "drago@vuckovic.co";
    String password = "passW0rd#";
    String company = "Company8";
    String subdomain = "company.pegasus.com";
    SignUpRequest request =
        SignUpRequest.builder()
            .firstname(firstname)
            .lastname(lastname)
            .email(email)
            .password(password)
            .company(company)
            .subdomain(subdomain)
            .build();
    SubscriptionPackageEntity subscriptionPackageEntity = new SubscriptionPackageEntity(1, "test",
        SubscriptionType.DEFAULT, 1024.00,null);
    subscriptionPackageEntityRepository.save(subscriptionPackageEntity);

    ResponseEntity<SignUpResponse> response = authController.signUp(request);
    List<UserEntity> users = userEntityRepository.findAll();
    List<TenantEntity> tenants = tenantEntityRepository.findAll();

    assertThat(users).hasSize(1);
    assertThat(tenants).hasSize(1);

    TenantEntity tenantEntity = tenants.get(0);
    assertThat(tenantEntity.getCompany()).isEqualTo(company);
    assertThat(tenantEntity.getSubdomain()).isEqualTo(subdomain);
    assertThat(tenantEntity.getTimestamp()).isNotNull();
    assertThat(tenantEntity.getStatus()).isEqualTo(TenantStatus.UNVERIFIED);

    UserEntity userEntity = users.get(0);
    assertThat(userEntity.getFirstname()).isEqualTo(firstname);
    assertThat(userEntity.getLastname()).isEqualTo(lastname);
    assertThat(userEntity.getEmail()).isEqualTo(email);
    assertThat(userEntity.getVerificationCode()).isNotNull();
    assertThat(userEntity.getEnabled()).isFalse();
    assertThat(userEntity.getRole()).isEqualTo(Role.OWNER);
    assertThat(userEntity.getTenantId()).isEqualTo(tenantEntity.getId());
    assertThat(Objects.requireNonNull(response.getBody()).getUserId())
        .isEqualTo(userEntity.getId());

    File testFile = new File("C:/root/" + File.separator + "Company8");
    assertTrue(testFile.exists());
  }

  @Test
  void signUp_twoTimesWithSameCompanyName_shouldThrowConflictException() {
    String firstname1 = "Test";
    String lastname1 = "Test";
    String email1 = "drago@vuckovic.co";
    String password1 = "passW0rd#";
    String company1 = "Company2";
    String subdomain1 = "company.pegasus.com";
    SignUpRequest request1 =
        SignUpRequest.builder()
            .firstname(firstname1)
            .lastname(lastname1)
            .email(email1)
            .password(password1)
            .company(company1)
            .subdomain(subdomain1)
            .build();
    SubscriptionPackageEntity subscriptionPackageEntity = new SubscriptionPackageEntity(1, "test",
        SubscriptionType.DEFAULT, 1024.00,null);
    subscriptionPackageEntityRepository.save(subscriptionPackageEntity);

    authController.signUp(request1);

    String firstname2 = "Test";
    String lastname2 = "Test";
    String email2 = "drago@vuckovic.co";
    String password2 = "passW0rd#";
    String company2 = "Company4";
    String subdomain2 = "company.pegasus.com";
    SignUpRequest request2 =
        SignUpRequest.builder()
            .firstname(firstname2)
            .lastname(lastname2)
            .email(email2)
            .password(password2)
            .company(company2)
            .subdomain(subdomain2)
            .build();

    assertThatThrownBy(() -> authController.signUp(request2)).isInstanceOf(ConflictException.class);
  }

  @Test
  void verifyMail_withValidData_shouldUpdateUserAndTenantEntity() {
    String firstname = "Test";
    String lastname = "Test";
    String email = "drago@vuckovic.co";
    String password = "passW0rd#";
    String company = "Company3";
    String subdomain = "company.pegasus.com";
    SignUpRequest request =
        SignUpRequest.builder()
            .firstname(firstname)
            .lastname(lastname)
            .email(email)
            .password(password)
            .company(company)
            .subdomain(subdomain)
            .build();
    SubscriptionPackageEntity subscriptionPackageEntity = new SubscriptionPackageEntity(1, "test",
        SubscriptionType.DEFAULT, 1024.00,null);
    subscriptionPackageEntityRepository.save(subscriptionPackageEntity);

    Mockito.doNothing().when(restTemplate.postForObject(
        String.format(
            "%s/create-total-execution-time/%s",
            lambdaProperties.getBaseUrl(), 1),
        null,
        Integer.class));

    authController.signUp(request);

    List<UserEntity> users = userEntityRepository.findAll();
    List<TenantEntity> tenants;
    UserEntity userEntity = users.get(0);

    Integer userId = userEntity.getId();
    String verificationCode = userEntity.getVerificationCode();
    VerifyEmailRequest verifyEmailRequest =
        new VerifyEmailRequest(userId, verificationCode, company);
    authController.verifyEmail(verifyEmailRequest);

    users = userEntityRepository.findAll();
    tenants = tenantEntityRepository.findAll();
    userEntity = users.get(0);
    TenantEntity tenantEntity = tenants.get(0);

    assertThat(userEntity.getVerificationCode()).isNull();
    assertThat(userEntity.getEnabled()).isTrue();
    assertThat(tenantEntity.getStatus()).isEqualTo(TenantStatus.VERIFIED);
  }

  @Test
  void verifyEmail_withInvalidData_shouldThrowNotFoundException() {
    String firstname = "Test";
    String lastname = "Test";
    String email = "drago@vuckovic.co";
    String password = "passW0rd#";
    String company = "Company6";
    String subdomain = "company.pegasus.com";
    SignUpRequest request =
        SignUpRequest.builder()
            .firstname(firstname)
            .lastname(lastname)
            .email(email)
            .password(password)
            .company(company)
            .subdomain(subdomain)
            .build();
    SubscriptionPackageEntity subscriptionPackageEntity = new SubscriptionPackageEntity(1, "test",
        SubscriptionType.DEFAULT, 1024.00,null);
    subscriptionPackageEntityRepository.save(subscriptionPackageEntity);

    authController.signUp(request);

    List<UserEntity> users = userEntityRepository.findAll();
    List<TenantEntity> tenants = tenantEntityRepository.findAll();
    UserEntity userEntity = users.get(0);

    Integer userId = userEntity.getId();
    String verificationCode = "wrongCode";

    VerifyEmailRequest verifyEmailRequest =
        new VerifyEmailRequest(userId, verificationCode, company);
    assertThatThrownBy(() -> authController.verifyEmail(verifyEmailRequest))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void login_withValidData_shouldReturnLoginResponse() {
    SubscriptionPackageEntity subscriptionPackageEntity = new SubscriptionPackageEntity(1, "test",
        SubscriptionType.DEFAULT, 1024.00,null);
    subscriptionPackageEntity = subscriptionPackageEntityRepository.save(subscriptionPackageEntity);
    TenantEntity tenantEntity =
        new TenantEntity(
            0,
            "test",
            "test.pegasus.com",
            TenantStatus.VERIFIED,
            new Timestamp(System.currentTimeMillis()), null, null, subscriptionPackageEntity,0);
    tenantEntity = tenantEntityRepository.save(tenantEntity);
    UserEntity userEntity =
        new UserEntity(
            0,
            "test",
            "test",
            "mail@mail.com",
            "$2a$10$e0ylxxthdWWfc3xdqzeCeujRHAZFVTT6uzqnhz4Cu8wui95MdDzQm",
            Role.OWNER,
            null,
            true,
            tenantEntity.getId());
    userEntity = userEntityRepository.save(userEntity);
    StatisticEntity statisticEntity = new StatisticEntity(1, 3, 4, 5);
    statisticEntity = statisticEntityRepository.save(statisticEntity);
    BucketEntity bucketEntity = new BucketEntity(1, 0.0, 1024.0, "stih", tenantEntity.getId(),
        statisticEntity);
    bucketEntityRepository.save(bucketEntity);
    LoginRequest loginRequest = new LoginRequest(userEntity.getEmail(), "passW0rd#");
    ResponseEntity<LoginResponse> response = authController.login(loginRequest);

    LoginResponse responseObject = response.getBody();
    assert responseObject != null;
    assertThat(responseObject.getEmail()).isEqualTo(loginRequest.getEmail());
    assertThat(responseObject.getToken()).isNotNull();
    assertThat(responseObject.getRefreshToken()).isNotNull();
    assertThat(responseObject.getRole()).isEqualTo(Role.OWNER);
    assertThat(responseObject.getTenantId()).isEqualTo(tenantEntity.getId());
    assertThat(responseObject.getCompany()).isEqualTo(tenantEntity.getCompany());
  }

  @Test
  void login_withInvalidData_shouldThrowNotFoundException() {
    SubscriptionPackageEntity subscriptionPackageEntity = new SubscriptionPackageEntity(1, "test",
        SubscriptionType.DEFAULT, 1024.00,null);
    subscriptionPackageEntity = subscriptionPackageEntityRepository.save(subscriptionPackageEntity);
    TenantEntity tenantEntity =
        new TenantEntity(
            0,
            "test",
            "test.pegasus.com",
            TenantStatus.VERIFIED,
            new Timestamp(System.currentTimeMillis()), null, null, subscriptionPackageEntity,0);
    tenantEntity = tenantEntityRepository.save(tenantEntity);
    UserEntity userEntity =
        new UserEntity(
            0,
            "test",
            "test",
            "mail@mail.com",
            "$2a$10$ZRekLsIaysJsfF9v.YU0wOCmJr0pWMs25MDq0C0dN7Fazacz7/Gc.",
            Role.OWNER,
            null,
            true,
            tenantEntity.getId());
    userEntityRepository.save(userEntity);

    LoginRequest loginRequest = new LoginRequest("fejkMejl@mail.com", "passW0rd#");
    assertThatThrownBy(() -> authController.login(loginRequest))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void forgottenPassword_withValidData_shouldSetVerificationCode() {
    SubscriptionPackageEntity subscriptionPackageEntity = new SubscriptionPackageEntity(1, "test",
        SubscriptionType.DEFAULT, 1024.00,null);
    subscriptionPackageEntity = subscriptionPackageEntityRepository.save(subscriptionPackageEntity);
    String email = "mail@mail.com";
    ForgotPasswordRequest forgotPasswordRequest =
        ForgotPasswordRequest.builder().email(email).build();
    TenantEntity tenantEntity =
        new TenantEntity(
            0,
            "test",
            "test.pegasus.com",
            TenantStatus.VERIFIED,
            new Timestamp(System.currentTimeMillis()), null, null, subscriptionPackageEntity,0);
    tenantEntity = tenantEntityRepository.save(tenantEntity);
    UserEntity userEntity =
        new UserEntity(
            0,
            "test",
            "test",
            "mail@mail.com",
            "$2a$10$ZRekLsIaysJsfF9v.YU0wOCmJr0pWMs25MDq0C0dN7Fazacz7/Gc.",
            Role.OWNER,
            null,
            true,
            tenantEntity.getId());
    userEntityRepository.save(userEntity);
    authController.processForgottenPassword(forgotPasswordRequest);
    List<UserEntity> users = userEntityRepository.findAll();
    UserEntity user = users.get(0);
    assertThat(user.getVerificationCode()).isNotNull();
  }

  @Test
  void forgottenPassword_withInvalidData_shouldReturnEmailNotFoundException() {
    SubscriptionPackageEntity subscriptionPackageEntity = new SubscriptionPackageEntity(1, "test",
        SubscriptionType.DEFAULT, 1024.00,null);
    subscriptionPackageEntity = subscriptionPackageEntityRepository.save(subscriptionPackageEntity);
    TenantEntity tenantEntity =
        new TenantEntity(
            0,
            "test",
            "test.pegasus.com",
            TenantStatus.VERIFIED,
            new Timestamp(System.currentTimeMillis()), null, null, subscriptionPackageEntity,0);
    tenantEntity = tenantEntityRepository.save(tenantEntity);
    UserEntity userEntity =
        new UserEntity(
            0,
            "test",
            "test",
            "mail@mail.com",
            "$2a$10$ZRekLsIaysJsfF9v.YU0wOCmJr0pWMs25MDq0C0dN7Fazacz7/Gc.",
            Role.OWNER,
            null,
            true,
            tenantEntity.getId());
    userEntityRepository.save(userEntity);
    String email = "uknownmail@mail.com";
    ForgotPasswordRequest forgotPasswordRequest =
        ForgotPasswordRequest.builder().email(email).build();
    assertThatThrownBy(() -> authController.processForgottenPassword(forgotPasswordRequest))
        .isInstanceOf(EmailNotFoundException.class);
  }

  @Test
  void resetPassword_withValidData_shouldSetVerficiationCodeToNull() {
    SubscriptionPackageEntity subscriptionPackageEntity = new SubscriptionPackageEntity(1, "test",
        SubscriptionType.DEFAULT, 1024.00,null);
    subscriptionPackageEntity = subscriptionPackageEntityRepository.save(subscriptionPackageEntity);
    String verificationCode = "12345";
    String password = "$2a$10$ZRekLsIaysJsfF9v.YU0wOCmJr0pWMs25MDq0C0dN7Fazacz7/Gc.";
    RecoveryPasswordRequest recoveryPasswordRequest =
        RecoveryPasswordRequest.builder()
            .verificationCode(verificationCode)
            .password(password)
            .build();
    TenantEntity tenantEntity =
        new TenantEntity(
            0,
            "test",
            "test.pegasus.com",
            TenantStatus.VERIFIED,
            new Timestamp(System.currentTimeMillis()), null, null, subscriptionPackageEntity,0);
    tenantEntity = tenantEntityRepository.save(tenantEntity);
    UserEntity userEntity =
        new UserEntity(
            0,
            "test",
            "test",
            "mail@mail.com",
            "$2a$10$ZRekLsIaysJsfF9v.YU0wOCmJr0pWMs25MDq0C0dN7Fazacz7/Gc.",
            Role.OWNER,
            "12345",
            true,
            tenantEntity.getId());
    userEntityRepository.save(userEntity);
    authController.processResetPassword(recoveryPasswordRequest);
    List<UserEntity> users = userEntityRepository.findAll();
    UserEntity user = users.get(0);
    assertThat(user.getVerificationCode()).isNull();
  }

  @Test
  void resetPassword_withInvalidData_shouldReturnNotFoundException() {
    SubscriptionPackageEntity subscriptionPackageEntity = new SubscriptionPackageEntity(1, "test",
        SubscriptionType.DEFAULT, 1024.00,null);
    subscriptionPackageEntity = subscriptionPackageEntityRepository.save(subscriptionPackageEntity);
    TenantEntity tenantEntity =
        new TenantEntity(
            0,
            "test",
            "test.pegasus.com",
            TenantStatus.VERIFIED,
            new Timestamp(System.currentTimeMillis()), null, null, subscriptionPackageEntity,0);
    tenantEntity = tenantEntityRepository.save(tenantEntity);
    UserEntity userEntity =
        new UserEntity(
            0,
            "test",
            "test",
            "mail@mail.com",
            "$2a$10$ZRekLsIaysJsfF9v.YU0wOCmJr0pWMs25MDq0C0dN7Fazacz7/Gc.",
            Role.OWNER,
            "1312",
            true,
            tenantEntity.getId());
    userEntityRepository.save(userEntity);
    String verificationCode = "12345";
    String password = "$2a$10$ZRekLsIaysJsfF9v.YU0wOCmJr0pWMs25MDq0C0dN7Fazacz7/Gc.";
    RecoveryPasswordRequest recoveryPasswordRequest =
        RecoveryPasswordRequest.builder()
            .verificationCode(verificationCode)
            .password(password)
            .build();
    assertThatThrownBy(() -> authController.processResetPassword(recoveryPasswordRequest))
        .isInstanceOf(NotFoundException.class);
  }
}
