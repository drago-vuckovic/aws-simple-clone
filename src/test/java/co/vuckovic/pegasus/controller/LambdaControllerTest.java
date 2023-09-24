package co.vuckovic.pegasus.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.vuckovic.pegasus.api.v1.controller.InvitationController;
import co.vuckovic.pegasus.common.WebMvcTestConfig;
import co.vuckovic.pegasus.model.enumeration.TriggerType;
import co.vuckovic.pegasus.model.request.LambdaCreationRequest;
import co.vuckovic.pegasus.service.InvitationService;
import co.vuckovic.pegasus.service.JwtUserDetailsService;
import co.vuckovic.pegasus.service.LambdaService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = InvitationController.class)
@WebMvcTestConfig
@AutoConfigureMockMvc(addFilters = false)
class LambdaControllerTest {

  @MockBean private LambdaService lambdaService;

  @MockBean private InvitationService invitationService;

  @MockBean private JwtUserDetailsService jwtUserDetailsService;

  @MockBean private PasswordEncoder passwordEncoder;

  @Autowired private MockMvc mockMvc;

  @Test
  void uploadFile_withFalseData_shouldReturnStatusOK() throws Exception {
    LambdaCreationRequest lambdaCreationRequest =
        LambdaCreationRequest.builder()
            .destFolderId(1)
            .srcFolderId(2)
            .name("test")
            .triggerType(TriggerType.UPLOAD)
            .build();

    Mockito.when(lambdaService.createLambda(null, lambdaCreationRequest)).thenReturn(1);
    mockMvc
        .perform(
            post("/api/lambda/create")
                .content( new org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(lambdaCreationRequest))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isNotFound());
  }
}
