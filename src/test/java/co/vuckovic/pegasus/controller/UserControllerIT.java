package co.vuckovic.pegasus.controller;

import static org.assertj.core.api.Assertions.assertThat;

import co.vuckovic.pegasus.api.v1.controller.UserController;
import co.vuckovic.pegasus.model.dto.User;
import co.vuckovic.pegasus.common.util.ContainerBaseTest.ContainerBaseTestInitializer;
import co.vuckovic.pegasus.model.enumeration.Role;
import co.vuckovic.pegasus.model.enumeration.SubscriptionType;
import co.vuckovic.pegasus.model.enumeration.TenantStatus;
import co.vuckovic.pegasus.repository.SubscriptionPackageEntityRepository;
import co.vuckovic.pegasus.repository.TenantEntityRepository;
import co.vuckovic.pegasus.repository.UserEntityRepository;
import co.vuckovic.pegasus.repository.entity.SubscriptionPackageEntity;
import co.vuckovic.pegasus.repository.entity.TenantEntity;
import co.vuckovic.pegasus.repository.entity.UserEntity;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {ContainerBaseTestInitializer.class})
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
class UserControllerIT {

  @Autowired
  private UserController userController;

  @Autowired
  private UserEntityRepository userEntityRepository;

  @Autowired
  private TenantEntityRepository tenantEntityRepository;

  @Autowired
  private SubscriptionPackageEntityRepository subscriptionPackageEntityRepository;

  @BeforeEach
  private void cleanUpBeforeEach() {
    userEntityRepository.deleteAll();
    tenantEntityRepository.deleteAll();
    subscriptionPackageEntityRepository.deleteAll();
  }

  @AfterAll
  private void cleanUpAfter() {
    userEntityRepository.deleteAll();
    tenantEntityRepository.deleteAll();
    subscriptionPackageEntityRepository.deleteAll();
  }

  @Test
  void getActiveUsersByTenantId_withValidData_shouldReturnListOfUsersByTenantId() {
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

    UserEntity userEntity2 =
        new UserEntity(
            1,
            "test2",
            "test2",
            "mail2@mail.com",
            "$1a$10$ZRekLsIaysJsfF9v.YU0wOCmJr0pWMs25MDq0C0dN7Fazacz7/Gc.",
            Role.OWNER,
            null,
            true,
            tenantEntity.getId());
    userEntityRepository.save(userEntity2);

    List<UserEntity> users = userEntityRepository.findAll();
    List<TenantEntity> tenants = tenantEntityRepository.findAll();

    assertThat(users).hasSize(2);
    assertThat(tenants).hasSize(1);

    ResponseEntity<List<User>> response =
        userController.getActiveUsersByTenantId(tenantEntity.getId());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
