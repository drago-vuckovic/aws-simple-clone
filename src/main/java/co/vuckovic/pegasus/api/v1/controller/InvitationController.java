package co.vuckovic.pegasus.api.v1.controller;

import co.vuckovic.pegasus.model.dto.Invitation;
import co.vuckovic.pegasus.model.request.AcceptInviteRequest;
import co.vuckovic.pegasus.model.request.InviteUserRequest;
import co.vuckovic.pegasus.model.response.InviteUserResponse;
import co.vuckovic.pegasus.model.response.LoginResponse;
import co.vuckovic.pegasus.service.InvitationService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

  private final InvitationService invitationService;

  @GetMapping("/{tenantId}")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Successful request"),
          @ApiResponse(responseCode = "500", description = "Internal error"),
          @ApiResponse(responseCode = "403", description = "Forbidden"),
          @ApiResponse(responseCode = "404", description = "Not found")
      })
  public ResponseEntity<List<Invitation>> getAllInvitationsByTenantId(
      @PathVariable Integer tenantId) {
    return ResponseEntity.ok(invitationService.getAllInvitationsByTenantId(tenantId));
  }

  @PostMapping
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Successfull request"),
          @ApiResponse(responseCode = "500", description = "Internal error"),
          @ApiResponse(responseCode = "400", description = "Bad request"),
          @ApiResponse(responseCode = "403", description = "Forbidden"),
          @ApiResponse(responseCode = "404", description = "Not found"),
          @ApiResponse(responseCode = "409", description = "Conflict")
      })
  public ResponseEntity<InviteUserResponse> createInvitation(
      @RequestBody @Valid InviteUserRequest inviteUserRequest) {
    return ResponseEntity.ok(invitationService.createInvitation(inviteUserRequest));
  }

  @GetMapping("/{invitationId}/resend-invite")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Successfull request"),
          @ApiResponse(responseCode = "500", description = "Internal error"),
          @ApiResponse(responseCode = "403", description = "Forbidden"),
          @ApiResponse(responseCode = "404", description = "Not found")
      })
  public ResponseEntity<Void> resendInvite(
      @PathVariable Integer invitationId) {
    invitationService.resendInvite(invitationId);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @GetMapping("/cancel/{invitationId}")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Successful request"),
          @ApiResponse(responseCode = "500", description = "Internal error"),
          @ApiResponse(responseCode = "403", description = "Forbidden"),
          @ApiResponse(responseCode = "404", description = "Not found")
      })
  public ResponseEntity<Void> cancelInvitation(@PathVariable Integer invitationId) {
    invitationService.cancelInvitation(invitationId);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @PostMapping("/accept-invite")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Successful request"),
          @ApiResponse(responseCode = "500", description = "Internal error"),
          @ApiResponse(responseCode = "400", description = "Bad request"),
          @ApiResponse(responseCode = "403", description = "Forbidden"),
          @ApiResponse(responseCode = "404", description = "Not found")
      })
  public ResponseEntity<LoginResponse> acceptInvite(
      @RequestBody @Valid AcceptInviteRequest acceptInviteRequest) {
    return ResponseEntity.ok(invitationService.acceptInvite(acceptInviteRequest));
  }
}
