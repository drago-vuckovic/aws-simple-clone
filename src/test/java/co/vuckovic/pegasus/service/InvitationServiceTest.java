package co.vuckovic.pegasus.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import co.vuckovic.pegasus.util.JwtUtil;
import co.vuckovic.pegasus.common.BaseUnitTest;
import co.vuckovic.pegasus.common.WebMvcTestConfig;
import co.vuckovic.pegasus.model.enumeration.InvitationStatus;
import co.vuckovic.pegasus.model.enumeration.Role;
import co.vuckovic.pegasus.model.enumeration.SubscriptionType;
import co.vuckovic.pegasus.model.enumeration.TenantStatus;
import co.vuckovic.pegasus.model.request.AcceptInviteRequest;
import co.vuckovic.pegasus.model.response.LoginResponse;
import co.vuckovic.pegasus.repository.BucketEntityRepository;
import co.vuckovic.pegasus.repository.InvitationEntityRepository;
import co.vuckovic.pegasus.repository.TenantEntityRepository;
import co.vuckovic.pegasus.repository.UserEntityRepository;
import co.vuckovic.pegasus.repository.entity.BucketEntity;
import co.vuckovic.pegasus.repository.entity.InvitationEntity;
import co.vuckovic.pegasus.repository.entity.TenantEntity;
import co.vuckovic.pegasus.repository.entity.UserEntity;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@WebMvcTest(value = InvitationService.class)
@WebMvcTestConfig
@AutoConfigureMockMvc(addFilters = false)
@EnableWebMvc
/*@RunWith(PowerMockRunner.class)
@PrepareForTest(fullyQualifiedNames = "service.co.vuckovic.pegasus.InvitationService")*/
class InvitationServiceTest extends BaseUnitTest {

  @Autowired
  @InjectMocks
  private InvitationService invitationService;

  @MockBean
  private UserEntityRepository userEntityRepository;

  @MockBean
  private TenantEntityRepository tenantEntityRepository;

  @MockBean
  private InvitationEntityRepository invitationEntityRepository;

  @MockBean
  private ModelMapper modelMapper;

  @MockBean
  private MailService mailService;

  @MockBean
  private JwtUtil jwtUtil;

  @MockBean
  private BucketEntityRepository bucketEntityRepository;

  @Test
  void cancelInvitation_withValidData_shouldSetInvitationStatusToCancelled() {
    Mockito.when(invitationEntityRepository.findById(1))
        .thenReturn(
            Optional.of(
                new InvitationEntity(
                    1,
                    "test",
                    "test",
                    "test@mail.com",
                    Timestamp.valueOf(LocalDateTime.now()),
                    Role.USER,
                    InvitationStatus.PENDING,
                    "verificationCode123",
                    1)));

    invitationService.cancelInvitation(1);

    ArgumentCaptor<InvitationEntity> invitationCaptor =
        ArgumentCaptor.forClass(InvitationEntity.class);
    verify(invitationEntityRepository, times(1)).save(invitationCaptor.capture());

    assertThat(invitationCaptor.getValue().getStatus()).isEqualTo(InvitationStatus.CANCELLED);
  }

  @Test
  void acceptInvite_withValidData_shouldReturnLoginResponse() {
    AcceptInviteRequest acceptInviteRequest = new AcceptInviteRequest(1, "verCode", "passW0rd#");
    InvitationEntity invitationEntity = new InvitationEntity(acceptInviteRequest.getInvitationId(),
        "test", "test",
        "mail@mail.com",
        new Timestamp(System.currentTimeMillis()), Role.USER, InvitationStatus.PENDING,
        acceptInviteRequest.getVerificationCode(), 1);
    UserEntity userEntity = new UserEntity(null, "test", "test", "mail@mail.com", null, Role.USER,
        "verCode", null, 1);
    BucketEntity bucketEntity = new BucketEntity(1, 0.0, 1024.0, "stih", 1, null);

    Mockito.when(invitationEntityRepository.findByIdAndVerificationCode(
            acceptInviteRequest.getInvitationId(), acceptInviteRequest.getVerificationCode()))
        .thenReturn(
            Optional.of(invitationEntity));

    Mockito.when(modelMapper.map(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(userEntity).thenReturn(
            new LoginResponse(1, "test", "test", invitationEntity.getEmail(), Role.OWNER, "token",
                "refreshToken", 1,
                "company", 1, SubscriptionType.DEFAULT));

    Mockito.when(userEntityRepository.save(userEntity)).thenReturn(
        new UserEntity(1, "test", "test", "mail@mail.com", "hash", Role.USER, null, true, 1));

    Mockito.when(tenantEntityRepository.findById(1))
        .thenReturn(Optional.of(new TenantEntity(
            1,
            "test",
            "test.pegasus.com",
            TenantStatus.VERIFIED,
            new Timestamp(System.currentTimeMillis()), null, null, null,null)));
    Mockito.when(bucketEntityRepository.findBucketEntityByTenantId(userEntity.getTenantId()))
        .thenReturn(Optional.of(bucketEntity));
    List<InvitationEntity> list = new ArrayList<>();
    list.add(new InvitationEntity(2, "test", "test",
        "mail@mail.com",
        new Timestamp(System.currentTimeMillis()), Role.USER, InvitationStatus.PENDING,
        acceptInviteRequest.getVerificationCode(), 2));
    list.add(new InvitationEntity(3, "test", "test",
        "mail@mail.com",
        new Timestamp(System.currentTimeMillis()), Role.USER, InvitationStatus.PENDING,
        acceptInviteRequest.getVerificationCode(), 3));
    Mockito.when(invitationEntityRepository.getAllByEmail("mail@mail.com")).thenReturn(list);

    LoginResponse response = invitationService.acceptInvite(acceptInviteRequest);

    ArgumentCaptor<InvitationEntity> inviteCaptor = ArgumentCaptor.forClass(InvitationEntity.class);
    verify(invitationEntityRepository, times(3)).save(inviteCaptor.capture());

    ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
    verify(userEntityRepository, times(1)).save(userCaptor.capture());

    assertThat(inviteCaptor.getAllValues().get(0).getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
    assertThat(inviteCaptor.getAllValues().get(0).getVerificationCode()).isNull();

    assertThat(userCaptor.getValue().getVerificationCode()).isNull();
    assertThat(userCaptor.getValue().getEnabled()).isTrue();

    assertThat(inviteCaptor.getAllValues().get(1).getStatus()).isEqualTo(InvitationStatus.REJECTED);
    assertThat(inviteCaptor.getAllValues().get(1).getVerificationCode()).isNull();
    assertThat(inviteCaptor.getAllValues().get(2).getStatus()).isEqualTo(InvitationStatus.REJECTED);
    assertThat(inviteCaptor.getAllValues().get(2).getVerificationCode()).isNull();

    assertThat(response.getEmail()).isEqualTo(invitationEntity.getEmail());
  }

//  @Test
//  void setVerificationCodeTimestampAndSendInvitationMail_withValidData_shouldReturnUpdatedInvitationEntity()
//      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//    InvitationEntity invitationEntity = new InvitationEntity(1, "test", "test", "mail@mail.com",
//        null, Role.USER, InvitationStatus.PENDING, null, 1);
//    Mockito.when(modelMapper.map(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(
//        new Invitation(1, "test", "test", "mail@mail.com", Role.USER, "verCode",
//            new Timestamp(System.currentTimeMillis()), 1, InvitationStatus.PENDING));
//
//    doNothing().when(mailService).sendInvitationMail(ArgumentMatchers.any(), ArgumentMatchers.any());
//
//    Method method = InvitationService.class.getDeclaredMethod(
//        "setVerificationCodeTimestampAndSendInvitationMail", InvitationEntity.class);
//    method.setAccessible(true);
//    InvitationEntity entity = (InvitationEntity) method.invoke(invitationService, invitationEntity);
//
//    ArgumentCaptor<InvitationEntity> inviteCaptor = ArgumentCaptor.forClass(InvitationEntity.class);
//    verify(invitationEntityRepository, times(1)).save(inviteCaptor.capture());
//
//    assertThat(inviteCaptor.getValue().getId()).isNotZero();
//    assertThat(inviteCaptor.getValue().getVerificationCode()).isNotNull();
//    assertThat(inviteCaptor.getValue().getTimestamp()).isNotNull();
//  }

}
