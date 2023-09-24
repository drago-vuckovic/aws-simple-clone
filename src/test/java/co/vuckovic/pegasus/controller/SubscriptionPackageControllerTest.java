package co.vuckovic.pegasus.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.vuckovic.pegasus.api.v1.controller.SubscriptionPackageController;
import co.vuckovic.pegasus.model.dto.SubscriptionPackage;
import co.vuckovic.pegasus.common.BaseUnitTest;
import co.vuckovic.pegasus.common.WebMvcTestConfig;
import co.vuckovic.pegasus.service.SubscriptionPackageService;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(value = SubscriptionPackageController.class)
@WebMvcTestConfig
@AutoConfigureMockMvc(addFilters = false)
class SubscriptionPackageControllerTest extends BaseUnitTest {

  @MockBean
  private SubscriptionPackageService subscriptionPackageService;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void getAllSubscriptions_withValidData_shouldReturnStatusOk() throws Exception {
    SubscriptionPackage subscriptionPackage = new SubscriptionPackage();
    subscriptionPackage.setId(1);
    List<SubscriptionPackage> allSubscriptions = List.of(subscriptionPackage);
    given(subscriptionPackageService.getAllSubscriptionPackages()).willReturn(allSubscriptions);
    mockMvc.perform(get("/api/subscriptions")).andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.size()", Matchers.is(1)))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(subscriptionPackage.getId())));
  }
}
