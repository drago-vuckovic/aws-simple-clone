package co.vuckovic.pegasus.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import co.vuckovic.pegasus.common.model.TestAuthentication;
import co.vuckovic.pegasus.util.JwtUtil;
import co.vuckovic.pegasus.common.BaseUnitTest;
import co.vuckovic.pegasus.common.WebMvcTestConfig;
import co.vuckovic.pegasus.config.BucketProperties;
import co.vuckovic.pegasus.config.LambdaProperties;
import co.vuckovic.pegasus.model.enumeration.Role;
import co.vuckovic.pegasus.model.enumeration.SubscriptionType;
import co.vuckovic.pegasus.model.enumeration.TenantStatus;
import co.vuckovic.pegasus.model.request.LoginRequest;
import co.vuckovic.pegasus.model.request.VerifyEmailRequest;
import co.vuckovic.pegasus.model.response.LoginResponse;
import co.vuckovic.pegasus.repository.BucketEntityRepository;
import co.vuckovic.pegasus.repository.StatisticEntityRepository;
import co.vuckovic.pegasus.repository.SubscriptionPackageEntityRepository;
import co.vuckovic.pegasus.repository.TenantEntityRepository;
import co.vuckovic.pegasus.repository.UserEntityRepository;
import co.vuckovic.pegasus.repository.entity.BucketEntity;
import co.vuckovic.pegasus.repository.entity.SubscriptionPackageEntity;
import co.vuckovic.pegasus.repository.entity.TenantEntity;
import co.vuckovic.pegasus.repository.entity.UserEntity;

import java.sql.Timestamp;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@WebMvcTest(value = AuthService.class)
@WebMvcTestConfig
@AutoConfigureMockMvc(addFilters = false)
@EnableWebMvc
class AuthServiceTest extends BaseUnitTest {

  @Autowired
  @InjectMocks
  private AuthService authService;

  @MockBean
  private UserEntityRepository userEntityRepository;

  @MockBean
  private TenantEntityRepository tenantEntityRepository;

  @MockBean
  private SubscriptionPackageEntityRepository subscriptionPackageEntityRepository;

  @MockBean
  private ModelMapper modelMapper;

  @MockBean
  private MailService mailService;

  @MockBean
  private AuthenticationManager authenticationManager;

  @MockBean
  private JwtUtil jwtUtil;


  @MockBean
  private FileService fileService;

  @MockBean
  private BucketProperties bucketProperties;

  @MockBean
  private BucketEntityRepository bucketEntityRepository;

  @MockBean
  StatisticEntityRepository statisticEntityRepository;

  @MockBean
  RestTemplate restTemplate;

  @MockBean
  LambdaProperties lambdaProperties;

  @Test
  void
  verifyEmail_withValidData_shouldEnableUserAndSetVerificationCodeToNullAndUpdateTenantStatus() {
    VerifyEmailRequest verifyEmailRequest = new VerifyEmailRequest(1, "testCode1234", "test");
    Mockito.when(
            userEntityRepository.findByIdAndVerificationCode(
                verifyEmailRequest.getId(), verifyEmailRequest.getVerificationCode()))
        .thenReturn(
            Optional.of(
                new UserEntity(
                    1,
                    "test",
                    "test",
                    "test@mail.com",
                    "password",
                    Role.OWNER,
                    "testCode1234",
                    false,
                    1)));
    Mockito.when(
            tenantEntityRepository.findByIdAndCompany(
                verifyEmailRequest.getId(), verifyEmailRequest.getCompany()))
        .thenReturn(
            Optional.of(
                new TenantEntity(
                    1,
                    "test",
                    "test.pegasus.com",
                    TenantStatus.UNVERIFIED,
                    new Timestamp(System.currentTimeMillis()), null, null, null,null)));

    authService.verifyEmail(verifyEmailRequest);

    ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
    verify(userEntityRepository, times(1)).save(userCaptor.capture());

    assertThat(userCaptor.getValue().getVerificationCode()).isNull();
    assertThat(userCaptor.getValue().getEnabled()).isTrue();

    ArgumentCaptor<TenantEntity> tenantCaptor = ArgumentCaptor.forClass(TenantEntity.class);
    verify(tenantEntityRepository, times(1)).save(tenantCaptor.capture());

    assertThat(tenantCaptor.getValue().getStatus()).isEqualTo(TenantStatus.VERIFIED);
  }

  @Test
  void login_withValidData_shouldReturnLoginResponseWithValidData() {
    Integer id = 1;
    String email = "test@gmail.com";
    String password = "passW0rd";
    TestAuthentication testAuthentication = new TestAuthentication(id, email, password);
    UserEntity userEntity =
        new UserEntity(1, "test", "test", email, password, Role.OWNER, "null", true, 1);
    TenantEntity tenantEntity =
        new TenantEntity(
            1,
            "company",
            "subdomain.pegasus.com",
            TenantStatus.VERIFIED,
            new Timestamp(System.currentTimeMillis()), null, null,new SubscriptionPackageEntity(),null);
    BucketEntity bucketEntity = new BucketEntity(1, 0.0, 1024.0, "stih", 1, null);
    Mockito.when(
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)))
        .thenReturn(testAuthentication);
    Mockito.when(userEntityRepository.findById(id)).thenReturn(Optional.of(userEntity));
    Mockito.when(tenantEntityRepository.findById(userEntity.getTenantId()))
        .thenReturn(Optional.of(tenantEntity));
    Mockito.when(bucketEntityRepository.findBucketEntityByTenantId(tenantEntity.getId()))
        .thenReturn(Optional.of(bucketEntity));
    Mockito.when(modelMapper.map(userEntity, LoginResponse.class))
        .thenReturn(new LoginResponse(1, userEntity.getFirstname(), userEntity.getLastname(), email,
            Role.OWNER, "token", "refreshToken", 1, "company", 1, SubscriptionType.DEFAULT));
    LoginResponse response = authService.login(new LoginRequest(email, password));
    assertThat(response.getEmail()).isEqualTo(email);
    assertThat(response.getId()).isEqualTo(1);
    assertThat(response.getRole()).isEqualTo(Role.OWNER);
    assertThat(response.getBucketId()).isEqualTo(1);
  }
}
