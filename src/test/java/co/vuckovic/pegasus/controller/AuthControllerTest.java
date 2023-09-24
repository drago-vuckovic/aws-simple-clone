package co.vuckovic.pegasus.controller;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.vuckovic.pegasus.api.v1.controller.AuthController;
import com.fasterxml.jackson.databind.ObjectMapper;
import co.vuckovic.pegasus.common.BaseUnitTest;
import co.vuckovic.pegasus.common.WebMvcTestConfig;
import co.vuckovic.pegasus.model.enumeration.Role;
import co.vuckovic.pegasus.model.enumeration.SubscriptionType;
import co.vuckovic.pegasus.model.request.ForgotPasswordRequest;
import co.vuckovic.pegasus.model.request.LoginRequest;
import co.vuckovic.pegasus.model.request.RecoveryPasswordRequest;
import co.vuckovic.pegasus.model.request.SignUpRequest;
import co.vuckovic.pegasus.model.request.VerifyEmailRequest;
import co.vuckovic.pegasus.model.response.LoginResponse;
import co.vuckovic.pegasus.model.response.SignUpResponse;
import co.vuckovic.pegasus.service.AuthService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = AuthController.class)
@WebMvcTestConfig
class AuthControllerTest extends BaseUnitTest {

  @MockBean
  private AuthService authService;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void verifyEmail_withValidData_shouldReturnStatusCode200() throws Exception {
    VerifyEmailRequest verifyEmailRequest = new VerifyEmailRequest(1, "testCode123456789", "test");

    doNothing().when(authService).verifyEmail(verifyEmailRequest);
    mockMvc
        .perform(
            post("/api/auth/verify-email")
                .content(new ObjectMapper().writeValueAsString(verifyEmailRequest))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk());
  }

  @Test
  void signUp_withValidData_shouldReturnStatusCode200() throws Exception {
    SignUpRequest request =
        new SignUpRequest(
            "luka", "el lukic", "bbooja@ds.com", "Pass@123dfdsf", "hhtte", "ccoompan.pegasus.com");

    Mockito.when(authService.signUp(request)).thenReturn(new SignUpResponse(1));
    mockMvc
        .perform(
            post("/api/auth/sign-up")
                .content(new ObjectMapper().writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk());
  }

  @Test
  void forgottenPassword_withValidData_shouldReturnStatusCode200() throws Exception {
    ForgotPasswordRequest forgotPasswordRequest =
        new ForgotPasswordRequest("drago@vuckovic.co");
    doNothing().when(authService).forgottenPassword(forgotPasswordRequest);
    mockMvc
        .perform(
            post("/api/auth/forgotten-password")
                .content(new ObjectMapper().writeValueAsString(forgotPasswordRequest))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  void recoverPassword_withValidData_shouldReturnStatusCode200() throws Exception {
    RecoveryPasswordRequest recoveryPasswordRequest =
        new RecoveryPasswordRequest("sdafdsafsdfdsaf", "sdfdfsdfsdf@ASD32");
    doNothing().when(authService).recoverPassword(recoveryPasswordRequest);
    mockMvc
        .perform(
            post("/api/auth/reset-password")
                .content(new ObjectMapper().writeValueAsString(recoveryPasswordRequest))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  void login_withValidData_shouldReturnStatusCode200() throws Exception {
    LoginRequest request = new LoginRequest("jpesevski99@gmail.com", "passW0rd");
    Mockito.when(authService.login(request))
        .thenReturn(
            new LoginResponse(
                1, "Jelena", "test", "jpesevski99@gmail.com", Role.OWNER, "jwtToken",
                "refreshToken", 1, "company", 1, SubscriptionType.DEFAULT));

    mockMvc
        .perform(
            post("/api/auth/login")
                .content(new ObjectMapper().writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk());
  }
}
