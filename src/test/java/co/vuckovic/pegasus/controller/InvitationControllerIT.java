package co.vuckovic.pegasus.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.vuckovic.pegasus.api.v1.controller.InvitationController;
import co.vuckovic.pegasus.common.util.ContainerBaseTest.ContainerBaseTestInitializer;
import co.vuckovic.pegasus.model.enumeration.InvitationStatus;
import co.vuckovic.pegasus.model.enumeration.Role;
import co.vuckovic.pegasus.model.enumeration.SubscriptionType;
import co.vuckovic.pegasus.model.enumeration.TenantStatus;
import co.vuckovic.pegasus.model.exception.NotFoundException;
import co.vuckovic.pegasus.model.request.ResendInvitationRequest;
import co.vuckovic.pegasus.repository.InvitationEntityRepository;
import co.vuckovic.pegasus.repository.SubscriptionPackageEntityRepository;
import co.vuckovic.pegasus.repository.TenantEntityRepository;
import co.vuckovic.pegasus.repository.entity.InvitationEntity;
import co.vuckovic.pegasus.repository.entity.SubscriptionPackageEntity;
import co.vuckovic.pegasus.repository.entity.TenantEntity;
import java.sql.Timestamp;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {ContainerBaseTestInitializer.class})
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
class InvitationControllerIT {

  @Autowired
  private InvitationController invitationController;
  @Autowired
  private InvitationEntityRepository invitationEntityRepository;
  @Autowired
  private TenantEntityRepository tenantEntityRepository;

  @Autowired
  private SubscriptionPackageEntityRepository subscriptionPackageEntityRepository;


  @BeforeEach
  private void cleanUpBeforeEach() {
    invitationEntityRepository.deleteAll();
    tenantEntityRepository.deleteAll();
    subscriptionPackageEntityRepository.deleteAll();
  }

  @AfterAll
  private void cleanUpAfter() {
    invitationEntityRepository.deleteAll();
    tenantEntityRepository.deleteAll();
    subscriptionPackageEntityRepository.deleteAll();
  }

  @Test
  void resendInvite_withValidData_shouldSetVerificationCode() {
    SubscriptionPackageEntity subscriptionPackageEntity = new SubscriptionPackageEntity(1, "test",
        SubscriptionType.DEFAULT, 1024.00,null);
    subscriptionPackageEntity = subscriptionPackageEntityRepository.save(subscriptionPackageEntity);
    TenantEntity tenantEntity =
        new TenantEntity(
            1,
            "test",
            "test.pegasus.com",
            TenantStatus.VERIFIED,
            new Timestamp(System.currentTimeMillis()), null, null, subscriptionPackageEntity,null);
    tenantEntity = tenantEntityRepository.save(tenantEntity);
    InvitationEntity invitation =
        new InvitationEntity(
            2,
            "bane",
            "banic",
            "jpesevski99@gmail.com",
            new Timestamp(System.currentTimeMillis()),
            Role.ADMIN,
            InvitationStatus.PENDING,
            "123",
            tenantEntity.getId());
    invitation = invitationEntityRepository.save(invitation);
    invitationController.resendInvite(invitation.getId());
    List<InvitationEntity> invites = invitationEntityRepository.findAll();
    InvitationEntity invite = invites.get(0);
    assertThat(invite.getVerificationCode()).isNotNull();
  }

  @Test
  void resendInvite_withInvalidData_shouldThrowNotFoundException() {
    SubscriptionPackageEntity subscriptionPackageEntity = new SubscriptionPackageEntity(1, "test",
        SubscriptionType.DEFAULT, 1024.00,null);
    subscriptionPackageEntity = subscriptionPackageEntityRepository.save(subscriptionPackageEntity);
    TenantEntity tenantEntity =
        new TenantEntity(
            1,
            "test",
            "test.pegasus.com",
            TenantStatus.VERIFIED,
            new Timestamp(System.currentTimeMillis()), null, null, subscriptionPackageEntity,null);
    tenantEntity = tenantEntityRepository.save(tenantEntity);
    InvitationEntity invitation =
        new InvitationEntity(
            1,
            "bane",
            "banic",
            "bane@gmail.com",
            new Timestamp(System.currentTimeMillis()),
            Role.ADMIN,
            InvitationStatus.PENDING,
            "123",
            tenantEntity.getId());
    invitationEntityRepository.save(invitation);
    Integer wrongID = 33;
    ResendInvitationRequest request = ResendInvitationRequest.builder().invitationId(wrongID)
        .build();
    assertThatThrownBy(() -> invitationController.resendInvite(request.getInvitationId()))
        .isInstanceOf(NotFoundException.class);
  }
}
