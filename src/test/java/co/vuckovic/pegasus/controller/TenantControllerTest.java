package co.vuckovic.pegasus.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.vuckovic.pegasus.api.v1.controller.TenantController;
import co.vuckovic.pegasus.common.BaseUnitTest;
import co.vuckovic.pegasus.common.WebMvcTestConfig;
import co.vuckovic.pegasus.model.enumeration.SubscriptionType;
import co.vuckovic.pegasus.model.response.ChangeSubscriptionResponse;
import co.vuckovic.pegasus.service.TenantService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = TenantController.class)
@WebMvcTestConfig
@AutoConfigureMockMvc(addFilters = false)
class TenantControllerTest extends BaseUnitTest {

  @MockBean
  private TenantService tenantService;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void changeSubscriptionPackage_withValidData_shouldReturnStatusCode200() throws Exception {
    Integer tenantId = 1;
    String subscriptionType = "SILVER";
    Mockito.when(tenantService.changeSubscriptionPackage(tenantId, subscriptionType))
        .thenReturn(new ChangeSubscriptionResponse(SubscriptionType.SILVER));

    mockMvc
        .perform(
            put("/api/tenants/" + tenantId).param("subscriptionType",
                subscriptionType))
        .andExpect(status().isOk());
  }
}
