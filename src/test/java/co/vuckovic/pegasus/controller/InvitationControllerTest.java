package co.vuckovic.pegasus.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.vuckovic.pegasus.api.v1.controller.InvitationController;
import co.vuckovic.pegasus.model.dto.Invitation;
import com.fasterxml.jackson.databind.ObjectMapper;
import co.vuckovic.pegasus.common.BaseUnitTest;
import co.vuckovic.pegasus.common.WebMvcTestConfig;
import co.vuckovic.pegasus.model.enumeration.InvitationStatus;
import co.vuckovic.pegasus.model.enumeration.Role;
import co.vuckovic.pegasus.model.enumeration.SubscriptionType;
import co.vuckovic.pegasus.model.request.AcceptInviteRequest;
import co.vuckovic.pegasus.model.request.InviteUserRequest;
import co.vuckovic.pegasus.model.request.ResendInvitationRequest;
import co.vuckovic.pegasus.model.response.InviteUserResponse;
import co.vuckovic.pegasus.model.response.LoginResponse;
import co.vuckovic.pegasus.service.InvitationService;
import java.sql.Timestamp;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(value = InvitationController.class)
@WebMvcTestConfig
@AutoConfigureMockMvc(addFilters = false)
class InvitationControllerTest extends BaseUnitTest {

  @MockBean
  private InvitationService invitationService;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void createInvitation_withValidData_shouldReturnStatusCode200AndInvitationId() throws Exception {
    InviteUserRequest inviteUserRequest = new InviteUserRequest("test", "test", "mail@mail.com",
        Role.USER, 1);

    Mockito.when(invitationService.createInvitation(inviteUserRequest))
        .thenReturn(
            new InviteUserResponse(1));
    MvcResult result = mockMvc
        .perform(
            post("/api/invitations")
                .content(new ObjectMapper().writeValueAsString(inviteUserRequest))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andReturn();

    InviteUserResponse expectedResponseBody = new InviteUserResponse(1);
    String actualResponseBody = result.getResponse().getContentAsString();
    assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
        new ObjectMapper().writeValueAsString(expectedResponseBody));
  }

  @Test
  void acceptInvitation_withValidData_shouldReturnStatusCode200AndLoginResponse() throws Exception {
    AcceptInviteRequest acceptInviteRequest = new AcceptInviteRequest(1, "verCode", "passW0rd#");

    Mockito.when(invitationService.acceptInvite(acceptInviteRequest))
        .thenReturn(
            new LoginResponse(1, "test", "test", "jpesevski99@gmail.com", Role.OWNER, "jwtToken",
                "refreshToken", 1,
                "company", 1, SubscriptionType.DEFAULT));

    MvcResult result = mockMvc.perform(post("/api/invitations/accept-invite").content(
                new ObjectMapper().writeValueAsString(acceptInviteRequest))
            .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andReturn();

    LoginResponse expectedResponse = new LoginResponse(1, "test", "test", "jpesevski99@gmail.com",
        Role.OWNER,
        "jwtToken", "refreshToken", 1,
        "company", 1, SubscriptionType.DEFAULT);
    String actualResponseBody = result.getResponse().getContentAsString();
    assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
        new ObjectMapper().writeValueAsString(expectedResponse));
  }

  @Test
  void getActiveInvitationsExceptAccepted_withValidData_shouldReturnListOfInvitations()
      throws Exception {

    when(invitationService.getAllInvitationsByTenantId(1))
        .thenReturn(
            List.of(
                new Invitation(
                    1,
                    "Sdfdddf",
                    "Dsfsdf",
                    "sdfsdf@gmail.com",
                    Role.USER,
                    "sdf",
                    new Timestamp(System.currentTimeMillis()),
                    1,
                    InvitationStatus.PENDING)));

    mockMvc
        .perform(get("/api/invitations/1"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.size()", Matchers.is(1)));
  }

  @Test
  void cancelInvitation_withValidData_shouldReturnStatusCode200() throws Exception {

    doNothing().when(invitationService).cancelInvitation(1);

    mockMvc
        .perform(
            get("/api/invitations/cancel/{invitationId}", 1)
        )
        .andExpect(status().isOk());
  }

  @Test
  void resendInvite_withValidData_shouldReturn200() throws Exception {
    ResendInvitationRequest request = new ResendInvitationRequest(1);
    doNothing().when(invitationService).resendInvite(request.getInvitationId());
    mockMvc.perform(
            get("/api/invitations/1/resend-invite")
                .content(new ObjectMapper().writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk());
  }
}
