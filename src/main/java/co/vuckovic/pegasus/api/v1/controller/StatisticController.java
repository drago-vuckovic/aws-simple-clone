package co.vuckovic.pegasus.api.v1.controller;

import co.vuckovic.pegasus.model.response.StatisticsResponse;
import co.vuckovic.pegasus.service.StatisticService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticController {

  private final StatisticService statisticService;

  @GetMapping("/{bucketId}")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Successful request"),
          @ApiResponse(responseCode = "500", description = "Internal error"),
          @ApiResponse(responseCode = "403", description = "Forbidden"),
          @ApiResponse(responseCode = "404", description = "Not found")
      })
  public ResponseEntity<StatisticsResponse> getStatistics(@PathVariable Integer bucketId) {
    return ResponseEntity.ok(statisticService.getStatistics(bucketId));
  }
}
