package co.vuckovic.pegasus.api.v1.controller;

import co.vuckovic.pegasus.model.dto.SubscriptionPackage;
import co.vuckovic.pegasus.service.SubscriptionPackageService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionPackageController {

  private final SubscriptionPackageService subscriptionPackageService;

  @GetMapping()
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Successful request"),
          @ApiResponse(responseCode = "500", description = "Internal error"),
          @ApiResponse(responseCode = "403", description = "Forbidden"),
          @ApiResponse(responseCode = "404", description = "Not found")
      })
  public ResponseEntity<List<SubscriptionPackage>> getAllSubscriptions() {
    return ResponseEntity.ok(subscriptionPackageService.getAllSubscriptionPackages());
  }
}
