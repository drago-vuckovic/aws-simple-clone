package co.vuckovic.pegasus.api.v1.controller;

import co.vuckovic.pegasus.model.request.ForgotPasswordRequest;
import co.vuckovic.pegasus.model.request.LoginRequest;
import co.vuckovic.pegasus.model.request.RecoveryPasswordRequest;
import co.vuckovic.pegasus.model.request.RefreshTokenRequest;
import co.vuckovic.pegasus.model.request.ResendEmailRequest;
import co.vuckovic.pegasus.model.request.SignUpRequest;
import co.vuckovic.pegasus.model.request.VerifyEmailRequest;
import co.vuckovic.pegasus.model.response.LoginResponse;
import co.vuckovic.pegasus.model.response.RefreshTokenResponse;
import co.vuckovic.pegasus.model.response.SignUpResponse;
import co.vuckovic.pegasus.service.AuthService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/sign-up")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Successfull request"),
          @ApiResponse(responseCode = "400", description = "Bad request"),
          @ApiResponse(responseCode = "404", description = "Not found")
      })
  public ResponseEntity<SignUpResponse> signUp(@RequestBody @Valid SignUpRequest request) {
    return ResponseEntity.ok(authService.signUp(request));
  }

  @PostMapping("/forgotten-password")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Successfull request"),
          @ApiResponse(responseCode = "500", description = "Internal error"),
          @ApiResponse(responseCode = "400", description = "Bad request"),
          @ApiResponse(responseCode = "403", description = "Forbidden"),
          @ApiResponse(responseCode = "404", description = "Not found")
      })
  public ResponseEntity<Void> processForgottenPassword(
      @RequestBody @Valid ForgotPasswordRequest forgotPasswordRequest) {
    authService.forgottenPassword(forgotPasswordRequest);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @PostMapping("/reset-password")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Successful request"),
          @ApiResponse(responseCode = "500", description = "Internal error"),
          @ApiResponse(responseCode = "400", description = "Bad request"),
          @ApiResponse(responseCode = "403", description = "Forbidden"),
          @ApiResponse(responseCode = "404", description = "Not found")
      })
  public ResponseEntity<Void> processResetPassword(
      @RequestBody @Valid RecoveryPasswordRequest recoveryPasswordRequest) {
    authService.recoverPassword(recoveryPasswordRequest);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @PostMapping("/resend-email")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Successfull request"),
          @ApiResponse(responseCode = "500", description = "Internal error"),
          @ApiResponse(responseCode = "400", description = "Bad request"),
          @ApiResponse(responseCode = "403", description = "Forbidden"),
          @ApiResponse(responseCode = "404", description = "Not found")
      })
  public ResponseEntity<Void> resendEmail(
      @RequestBody @Valid ResendEmailRequest resendEmailRequest) {
    authService.resendEmail(resendEmailRequest);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @PostMapping("/refresh-token")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Successfull request"),
          @ApiResponse(responseCode = "500", description = "Internal error"),
          @ApiResponse(responseCode = "400", description = "Bad request"),
          @ApiResponse(responseCode = "403", description = "Forbidden"),
          @ApiResponse(responseCode = "404", description = "Not found")
      })
  public ResponseEntity<RefreshTokenResponse> refreshToken(
      @RequestBody @Valid RefreshTokenRequest request) {
    return ResponseEntity.ok(authService.refreshToken(request));
  }

  @PostMapping("/verify-email")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Successfull request"),
          @ApiResponse(responseCode = "500", description = "Internal error"),
          @ApiResponse(responseCode = "400", description = "Bad request"),
          @ApiResponse(responseCode = "403", description = "Forbidden"),
          @ApiResponse(responseCode = "404", description = "Not found")
      })
  public ResponseEntity<Void> verifyEmail(
      @RequestBody @Valid VerifyEmailRequest verifyEmailRequest) {
    authService.verifyEmail(verifyEmailRequest);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/login")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Successfull request"),
          @ApiResponse(responseCode = "500", description = "Internal error"),
          @ApiResponse(responseCode = "400", description = "Bad request"),
          @ApiResponse(responseCode = "404", description = "Not found")
      })
  public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
    return ResponseEntity.ok(authService.login(loginRequest));
  }

}
