package co.vuckovic.pegasus.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.vuckovic.pegasus.model.dto.JwtUser;
import co.vuckovic.pegasus.model.dto.Tenant;
import co.vuckovic.pegasus.model.dto.User;
import co.vuckovic.pegasus.util.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import co.vuckovic.pegasus.common.BaseUnitTest;
import co.vuckovic.pegasus.common.WebMvcTestConfig;
import co.vuckovic.pegasus.model.enumeration.Role;
import co.vuckovic.pegasus.model.enumeration.TenantStatus;
import co.vuckovic.pegasus.model.request.ChangePasswordRequest;
import co.vuckovic.pegasus.model.request.UpdateCompanyNameRequest;
import co.vuckovic.pegasus.model.request.UpdateProfileDetailsRequest;
import co.vuckovic.pegasus.repository.BucketEntityRepository;
import co.vuckovic.pegasus.repository.TenantEntityRepository;
import co.vuckovic.pegasus.repository.UserEntityRepository;
import co.vuckovic.pegasus.repository.entity.BucketEntity;
import co.vuckovic.pegasus.repository.entity.StatisticEntity;
import co.vuckovic.pegasus.repository.entity.TenantEntity;
import co.vuckovic.pegasus.repository.entity.UserEntity;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@WebMvcTest(value = UserService.class)
@WebMvcTestConfig
@AutoConfigureMockMvc(addFilters = false)
@EnableWebMvc
class UserServiceTest extends BaseUnitTest {

  @Autowired
  @InjectMocks
  private UserService userService;

  @MockBean
  private UserEntityRepository userEntityRepository;

  @MockBean
  private TenantEntityRepository tenantEntityRepository;

  @MockBean
  private ModelMapper modelMapper;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private BucketEntityRepository bucketEntityRepository;

  @MockBean
  private JwtUtil jwtUtil;

  @MockBean
  private FileService fileService;

  @Test
  void getAllUsersByTenantID_ShouldReturnListOfUsersOfThatTenant() {
    User user =
        new User(1, "firstname", "lastname", "email@gmail.com", Role.USER, "safdsfds", false);
    Tenant tenant =
        new Tenant(
            1, "com", "com", TenantStatus.VERIFIED, new Timestamp(System.currentTimeMillis()));
    Mockito.when(userEntityRepository.findByTenantId(tenant.getId()))
        .thenReturn(
            (Optional.of(
                List.of(
                    new UserEntity(
                        1,
                        "firstname",
                        "lastname",
                        "email@gmail.com",
                        "pass@sdsaA2",
                        Role.USER,
                        "asdfdsf",
                        false,
                        1)))));
    List<User> userEntityList = userService.getAllUsersByTenantId(tenant.getId());
    assertThat(userEntityList).isNotNull();
    assertThat(userEntityList.size()).isEqualTo(1);
  }

  @Test
  void changeUserStatus_withValidData_shouldChangeUserStatus()
      throws IOException, JsonPatchException {
    Mockito.when(userEntityRepository.findById(1))
        .thenReturn(
            Optional.of(
                new UserEntity(
                    1,
                    "test",
                    "test",
                    "test@mail.com",
                    "password",
                    Role.ADMIN,
                    "testCode1234",
                    true,
                    1)));
    String json = " [ {\"op\":\"replace\",\"path\":\"/role\",\"value\":\"USER\"} ]";

    JsonNode jsonNode = objectMapper.readTree(json);

    JsonPatch jsonPatch = JsonPatch.fromJson(jsonNode);

    userService.changeUserRole(1, jsonPatch);

    ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
    verify(userEntityRepository, times(1)).save(userCaptor.capture());

    assertThat(userCaptor.getValue().getRole()).isEqualTo(Role.USER);
  }

  @Test
  void changePassword_withValidData_shouldChangePassword() {
    Mockito.when(userEntityRepository.findById(ArgumentMatchers.any()))
        .thenReturn(
            (Optional.of(
                new UserEntity(
                    1,
                    "firstname",
                    "lastname",
                    "email@gmail.com",
                    "pass@sdsaA2",
                    Role.USER,
                    null,
                    true,
                    1))));
    Mockito.when(
            passwordEncoder.matches(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
        .thenReturn(true);
    Mockito.when(passwordEncoder.encode(ArgumentMatchers.anyString()))
        .thenReturn("$2a$10$Fumh9NGzkwOS8nAR5oQzjONOQdqVCNPKU6VMrW2UOMd8gv0b4k0CS");
    ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest("pass@sdsaA2",
        "pass@sdsaA2nova");

    userService.changePassword(1, changePasswordRequest);

    ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
    verify(userEntityRepository, times(1)).save(userCaptor.capture());

    assertThat(userCaptor.getValue().getPassword()).isEqualTo(
        "$2a$10$Fumh9NGzkwOS8nAR5oQzjONOQdqVCNPKU6VMrW2UOMd8gv0b4k0CS");
  }

  @Test
  void updateProfileStatus_withValidData_shouldChangeUserProfileDetails() {
    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    JwtUser jwtUser = new JwtUser(1, "mail@mail.com", "password", Role.USER, true);

    SecurityContextHolder.setContext(securityContext);
    when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(jwtUser);

    Mockito.when(jwtUtil.getCurrentUser()).thenReturn(jwtUser);

    UpdateProfileDetailsRequest updateProfileDetailsRequest =
        UpdateProfileDetailsRequest.builder().firstname("Firstname").lastname("Lastname").build();

    UserEntity userEntity = new UserEntity(
        1,
        "test",
        "test",
        "test@mail.com",
        "password",
        Role.OWNER,
        "testCode1234",
        false,
        1);

    Mockito.when(userEntityRepository.findById(userEntity.getId()))
        .thenReturn(Optional.of(userEntity));

    userService.updateProfileDetails(1, updateProfileDetailsRequest);

    ArgumentCaptor<UserEntity> userCaptor =
        ArgumentCaptor.forClass(UserEntity.class);
    verify(userEntityRepository, times(1)).save(userCaptor.capture());

    assertThat(userEntity.getFirstname()).isEqualTo(updateProfileDetailsRequest.getFirstname());
    assertThat(userEntity.getLastname()).isEqualTo(updateProfileDetailsRequest.getLastname());
  }

  @Test
  void updateCompanyName_withValidData_shouldChangeTenantCompanyName() {
    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    JwtUser jwtUser = new JwtUser(1, "test@mail.com", "password", Role.OWNER, true);
    SecurityContextHolder.setContext(securityContext);
    when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(jwtUser);

    Mockito.when(jwtUtil.getCurrentUser()).thenReturn(jwtUser);

    UpdateCompanyNameRequest request = UpdateCompanyNameRequest.builder().company("noviNaziv")
        .build();

    UserEntity userEntity = new UserEntity(
        1,
        "test",
        "test",
        "test@mail.com",
        "password",
        Role.OWNER,
        "testCode1234",
        false,
        1);

    Mockito.when(userEntityRepository.findById(userEntity.getId()))
        .thenReturn(Optional.of(userEntity));

    TenantEntity tenantEntity = new TenantEntity(
        1,
        "test",
        "test.pegasus.com",
        TenantStatus.VERIFIED,
        new Timestamp(System.currentTimeMillis()), null, null, null,null);

    Mockito.when(tenantEntityRepository.findById(tenantEntity.getId()))
        .thenReturn(Optional.of(tenantEntity));

    BucketEntity bucketEntity = new BucketEntity(1, 1024.0, 2048.0, "test", 1,
        new StatisticEntity(1, 2, 3, 4));

    Mockito.when(bucketEntityRepository.findBucketEntityByTenantId(tenantEntity.getId()))
        .thenReturn(Optional.of(bucketEntity));

    userService.updateCompanyName(userEntity.getId(), request);

    ArgumentCaptor<TenantEntity> tenantCaptor =
        ArgumentCaptor.forClass(TenantEntity.class);
    verify(tenantEntityRepository, times(1)).save(tenantCaptor.capture());

    assertThat(tenantEntity.getCompany()).isEqualTo(request.getCompany());
  }
}
