package co.vuckovic.pegasus.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.vuckovic.pegasus.model.dto.JwtUser;
import co.vuckovic.pegasus.util.JwtUtil;
import co.vuckovic.pegasus.common.BaseUnitTest;
import co.vuckovic.pegasus.common.WebMvcTestConfig;
import co.vuckovic.pegasus.config.LambdaProperties;
import co.vuckovic.pegasus.model.enumeration.Role;
import co.vuckovic.pegasus.model.enumeration.SubscriptionType;
import co.vuckovic.pegasus.model.enumeration.TenantStatus;
import co.vuckovic.pegasus.model.exception.ActionNotAllowedException;
import co.vuckovic.pegasus.repository.BucketEntityRepository;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@WebMvcTest(value = TenantService.class)
@WebMvcTestConfig
@AutoConfigureMockMvc(addFilters = false)
@EnableWebMvc
class TenantServiceTest extends BaseUnitTest {

  @Autowired
  @InjectMocks
  private TenantService tenantService;

  @MockBean
  private UserEntityRepository userEntityRepository;

  @MockBean
  private TenantEntityRepository tenantEntityRepository;

  @MockBean
  private BucketEntityRepository bucketEntityRepository;

  @MockBean
  private SubscriptionPackageEntityRepository subscriptionPackageEntityRepository;

  @MockBean
  private JwtUtil jwtUtil;

  @MockBean
  RestTemplate restTemplate;

  @MockBean
  LambdaProperties lambdaProperties;

  @Test
  void changeSubscriptionPackage_withValidData_shouldChangeSubscriptionPackageAndBucketCapacityAndReturnNewSubscriptionType() {
    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    JwtUser jwtUser = new JwtUser(1, "mail@mail.com", "password", Role.OWNER, true);

    SecurityContextHolder.setContext(securityContext);
    when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(jwtUser);
    Mockito.when(jwtUtil.getCurrentUser()).thenReturn(jwtUser);

    Mockito.when(userEntityRepository.getById(jwtUser.getId())).thenReturn(new UserEntity(
        1,
        "test",
        "test",
        "mail@mail.com",
        "password",
        Role.OWNER,
        null,
        true,
        1));

    Mockito.when(tenantEntityRepository.findById(1)).thenReturn(Optional.of(new TenantEntity(
        1,
        "company",
        "subdomain.pegasus.com",
        TenantStatus.VERIFIED,
        new Timestamp(System.currentTimeMillis()), null, null,
        new SubscriptionPackageEntity(1, "opis", SubscriptionType.DEFAULT, 1048576.00,null),null)));

    SubscriptionPackageEntity newSubscription = new SubscriptionPackageEntity(2, "opis",
        SubscriptionType.SILVER, 10485760.00,null);
    Mockito.when(subscriptionPackageEntityRepository.findBySubscriptionType(newSubscription.getSubscriptionType()))
        .thenReturn(Optional.of(newSubscription));

    Mockito.when(bucketEntityRepository.findByTenantId(1))
        .thenReturn(Optional.of(new BucketEntity(1, 1.2, 1048576.0, "test", 1, null)));

    Integer tenantId = 1;
    tenantService.changeSubscriptionPackage(tenantId, newSubscription.getSubscriptionType().toString());

    ArgumentCaptor<BucketEntity> bucketCaptor =
        ArgumentCaptor.forClass(BucketEntity.class);
    verify(bucketEntityRepository, times(1)).save(bucketCaptor.capture());

    ArgumentCaptor<TenantEntity> tenantCaptor =
        ArgumentCaptor.forClass(TenantEntity.class);
    verify(tenantEntityRepository, times(1)).save(tenantCaptor.capture());

    assertThat(bucketCaptor.getValue().getCapacity()).isEqualTo(newSubscription.getCapacity());
    assertThat(
        tenantCaptor.getValue().getSubscriptionPackageEntity().getSubscriptionType()).isEqualTo(
        newSubscription.getSubscriptionType());
  }

  @Test
  void changeSubscriptionPackage_withDowngrade_shouldThrowActionNotAllowedException() {

    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    JwtUser jwtUser = new JwtUser(1, "mail@mail.com", "password", Role.OWNER, true);

    SecurityContextHolder.setContext(securityContext);
    when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(jwtUser);
    Mockito.when(jwtUtil.getCurrentUser()).thenReturn(jwtUser);

    Mockito.when(userEntityRepository.getById(jwtUser.getId())).thenReturn(new UserEntity(
        1,
        "test",
        "test",
        "mail@mail.com",
        "password",
        Role.OWNER,
        null,
        true,
        1));

    Mockito.when(tenantEntityRepository.findById(1)).thenReturn(Optional.of(new TenantEntity(
        1,
        "company",
        "subdomain.pegasus.com",
        TenantStatus.VERIFIED,
        new Timestamp(System.currentTimeMillis()), null, null,
        new SubscriptionPackageEntity(2, "opis", SubscriptionType.GOLD, 20971520.00,null),null)));

    SubscriptionPackageEntity newSubscription = new SubscriptionPackageEntity(1, "opis",
        SubscriptionType.SILVER, 10485760.00,null);
    Mockito.when(subscriptionPackageEntityRepository.findBySubscriptionType(newSubscription.getSubscriptionType()))
        .thenReturn(Optional.of(newSubscription));

    Mockito.when(bucketEntityRepository.findByTenantId(1))
        .thenReturn(Optional.of(new BucketEntity(1, 1.2, 1048576.0, "test", 1, null)));

    assertThatThrownBy(() -> tenantService.changeSubscriptionPackage(1, newSubscription.getSubscriptionType().toString()))
        .isInstanceOf(ActionNotAllowedException.class);
  }


}
