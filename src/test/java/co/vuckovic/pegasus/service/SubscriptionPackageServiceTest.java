package co.vuckovic.pegasus.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

import co.vuckovic.pegasus.model.dto.SubscriptionPackage;
import co.vuckovic.pegasus.model.dto.SubscriptionPackageList;
import co.vuckovic.pegasus.common.BaseUnitTest;
import co.vuckovic.pegasus.common.WebMvcTestConfig;
import co.vuckovic.pegasus.model.enumeration.SubscriptionType;
import co.vuckovic.pegasus.repository.SubscriptionPackageEntityRepository;
import co.vuckovic.pegasus.repository.entity.SubscriptionPackageEntity;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@WebMvcTest(value = SubscriptionPackageService.class)
@WebMvcTestConfig
@AutoConfigureMockMvc(addFilters = false)
@EnableWebMvc
class SubscriptionPackageServiceTest extends BaseUnitTest {

  @MockBean
  private SubscriptionPackageEntityRepository subscriptionPackageEntityRepository;

  @Autowired
  @InjectMocks
  private SubscriptionPackageService subscriptionPackageService;

  @MockBean
  private ModelMapper modelMapper;


  @Test
  void getAllSubscriptions_withValidData_shouldReturnAllSubscriptions() {
    List<SubscriptionPackageEntity> subscriptionEntities = new ArrayList<>();
    subscriptionEntities.add(
        new SubscriptionPackageEntity(1, "description", SubscriptionType.DEFAULT, 1024.0,null));
    subscriptionEntities.add(
        new SubscriptionPackageEntity(2, "description2", SubscriptionType.DEFAULT, 1024.0,null));
    given(subscriptionPackageEntityRepository.findAll()).willReturn(subscriptionEntities);

    List<SubscriptionPackage> expected = subscriptionPackageService.getAllSubscriptionPackages();
    List<SubscriptionPackage> subscriptions = modelMapper.map(subscriptionEntities,
        SubscriptionPackageList.class);
    assertEquals(expected, subscriptions);
  }
}
