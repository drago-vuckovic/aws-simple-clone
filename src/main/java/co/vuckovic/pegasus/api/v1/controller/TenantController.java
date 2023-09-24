package co.vuckovic.pegasus.api.v1.controller;

import co.vuckovic.pegasus.model.response.ChangeSubscriptionResponse;
import co.vuckovic.pegasus.service.TenantService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {

  private final TenantService tenantService;

  @PutMapping("/{tenantId}")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Successfull request"),
          @ApiResponse(responseCode = "400", description = "Bad request"),
          @ApiResponse(responseCode = "404", description = "Action not allowed")
      })
  public ResponseEntity<ChangeSubscriptionResponse> changeSubscriptionPackage(
      @PathVariable Integer tenantId, @RequestParam String subscriptionType) {
    return ResponseEntity.ok(
        tenantService.changeSubscriptionPackage(tenantId, subscriptionType));
  }
}
