package co.vuckovic.pegasus.api.v1.controller;

import co.vuckovic.pegasus.model.dto.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import co.vuckovic.pegasus.model.exception.NotFoundException;
import co.vuckovic.pegasus.model.request.ChangePasswordRequest;
import co.vuckovic.pegasus.model.request.UpdateCompanyNameRequest;
import co.vuckovic.pegasus.model.request.UpdateProfileDetailsRequest;
import co.vuckovic.pegasus.model.response.BucketInfoResponse;
import co.vuckovic.pegasus.model.response.LoggedInUserResponse;
import co.vuckovic.pegasus.model.response.UserInfoResponse;
import co.vuckovic.pegasus.service.UserService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping
  public ResponseEntity<?> test() {
    return ResponseEntity.ok("SDFASDF");
  }

  @GetMapping("/{tenantId}")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Successful request"),
          @ApiResponse(responseCode = "500", description = "Internal error"),
          @ApiResponse(responseCode = "403", description = "Forbidden"),
          @ApiResponse(responseCode = "404", description = "Not found")
      })
  public ResponseEntity<List<User>> getActiveUsersByTenantId(@PathVariable Integer tenantId) {
    return ResponseEntity.ok(userService.getAllUsersByTenantId(tenantId));
  }

  @GetMapping("/logged-in")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Successful request"),
          @ApiResponse(responseCode = "500", description = "Internal error")
      })
  public ResponseEntity<LoggedInUserResponse> getCurrentlyLoggedInUser(Authentication principal) {
    if (principal == null) {
      throw new NotFoundException("There is no logged in user.");
    }
    return ResponseEntity.ok(userService.getCurrentlyLoggedInUserByMail(principal.getName()));
  }

  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Successful request"),
          @ApiResponse(responseCode = "403", description = "Forbidden"),
          @ApiResponse(responseCode = "404", description = "Not found"),
          @ApiResponse(responseCode = "500", description = "Internal error")
      })
  @PatchMapping(value = "/{id}/status", consumes = "application/json-patch+json")
  public ResponseEntity<Void> changeUserStatus(
      @PathVariable Integer id, @RequestBody JsonPatch userJsonPatch)
      throws JsonProcessingException, com.github.fge.jsonpatch.JsonPatchException {
    userService.changeUserStatus(id, userJsonPatch);
    return ResponseEntity.ok().build();
  }

  @PutMapping("/{id}/profile-details")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Successful request"),
          @ApiResponse(responseCode = "500", description = "Internal error"),
          @ApiResponse(responseCode = "403", description = "Forbidden"),
          @ApiResponse(responseCode = "404", description = "Not found")
      })
  public ResponseEntity<Void> updateProfileDetails(
      @PathVariable Integer id, @Valid @RequestBody UpdateProfileDetailsRequest request) {
    userService.updateProfileDetails(id, request);
    return ResponseEntity.ok().build();
  }

  @PutMapping("/{id}/company-name")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Successful request"),
          @ApiResponse(responseCode = "500", description = "Internal error"),
          @ApiResponse(responseCode = "403", description = "Forbidden"),
          @ApiResponse(responseCode = "404", description = "Not found")
      })
  public ResponseEntity<Void> updateCompanyName(@PathVariable Integer id,
      @Valid @RequestBody UpdateCompanyNameRequest request) {
    userService.updateCompanyName(id, request);
    return ResponseEntity.ok().build();
  }


  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Successful request"),
          @ApiResponse(responseCode = "403", description = "Forbidden"),
          @ApiResponse(responseCode = "404", description = "Not found"),
          @ApiResponse(responseCode = "400", description = "Bad request"),
          @ApiResponse(responseCode = "500", description = "Internal error")
      })
  @PatchMapping(value = "/{id}/role", consumes = "application/json-patch+json")
  public ResponseEntity<Void> changeUserRole(
      @PathVariable Integer id, @RequestBody JsonPatch userJsonPatch)
      throws JsonProcessingException, com.github.fge.jsonpatch.JsonPatchException {
    userService.changeUserRole(id, userJsonPatch);
    return ResponseEntity.ok().build();
  }

  @PutMapping("/{id}/password")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Successful request"),
          @ApiResponse(responseCode = "403", description = "Forbidden"),
          @ApiResponse(responseCode = "404", description = "Not found"),
          @ApiResponse(responseCode = "400", description = "Bad request"),
          @ApiResponse(responseCode = "500", description = "Internal error")
      })
  public ResponseEntity<Void> changePassword(@PathVariable Integer id,
      @RequestBody @Valid ChangePasswordRequest changePasswordRequest) {
    userService.changePassword(id, changePasswordRequest);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/info")
  public ResponseEntity<UserInfoResponse> getUserInfo() {
    return ResponseEntity.ok(userService.getUserInfo());
  }

  @GetMapping("/bucket-info")
  public ResponseEntity<BucketInfoResponse> getBucketInfo() {
    return ResponseEntity.ok(userService.getBucketInfo());
  }
}
