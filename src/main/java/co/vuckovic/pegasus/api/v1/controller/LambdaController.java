package co.vuckovic.pegasus.api.v1.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import co.vuckovic.pegasus.model.request.ChangeLambdaFolderRequest;
import co.vuckovic.pegasus.model.request.LambdaCreationRequest;
import co.vuckovic.pegasus.model.request.LambdaUpdateRequest;
import co.vuckovic.pegasus.model.request.LambdaUpdateStatusRequest;
import co.vuckovic.pegasus.model.response.LambdaExecutionTimeResponse;
import co.vuckovic.pegasus.model.response.LambdasResponse;
import co.vuckovic.pegasus.service.LambdaService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/lambda")
@RequiredArgsConstructor
public class LambdaController {

  private final LambdaService lambdaService;

  private final ObjectMapper objectMapper;

  @PostMapping("/create")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Successful request"),
          @ApiResponse(responseCode = "403", description = "Forbidden"),
          @ApiResponse(responseCode = "404", description = "Not found"),
          @ApiResponse(responseCode = "500", description = "Internal error")
      })
  public ResponseEntity<Integer> uploadFile(
      @RequestParam("file") MultipartFile file,
      @RequestParam("lambda") String lambdaCreationJsonRequest)
      throws JsonProcessingException {

    LambdaCreationRequest lambdaCreationRequest =
        objectMapper.readValue(lambdaCreationJsonRequest, LambdaCreationRequest.class);
    return ResponseEntity.ok(lambdaService.createLambda(file, lambdaCreationRequest));
  }

  @PutMapping("/{id}")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Successful request"),
          @ApiResponse(responseCode = "403", description = "Forbidden"),
          @ApiResponse(responseCode = "404", description = "Not found"),
          @ApiResponse(responseCode = "500", description = "Internal error")
      })
  public ResponseEntity<Void> updateLambda(@PathVariable Integer id,
      @RequestParam(value = "file", required = false) MultipartFile file,
      @RequestParam(value = "lambda") String lambdaUpdateRequestString)
      throws JsonProcessingException {

    LambdaUpdateRequest lambdaUpdateRequest =
        objectMapper.readValue(lambdaUpdateRequestString, LambdaUpdateRequest.class);
    lambdaService.updateLambda(id, file, lambdaUpdateRequest);
    return ResponseEntity.ok().build();
  }

  @PutMapping("/{id}/status")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Successful request"),
          @ApiResponse(responseCode = "403", description = "Forbidden"),
          @ApiResponse(responseCode = "404", description = "Not found"),
          @ApiResponse(responseCode = "500", description = "Internal error")
      })
  public ResponseEntity<Void> updateLambdaStatus(
      @PathVariable Integer id, @RequestBody LambdaUpdateStatusRequest lambdaUpdateRequest) {
    lambdaService.updateLambdaStatus(id, lambdaUpdateRequest);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{id}")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Successful request"),
          @ApiResponse(responseCode = "403", description = "Forbidden"),
          @ApiResponse(responseCode = "404", description = "Not found"),
          @ApiResponse(responseCode = "500", description = "Internal error")
      })
  public ResponseEntity<Void> deleteLambda(@PathVariable Integer id) {
    lambdaService.deleteLambda(id);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{tenantId}")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Successful request"),
          @ApiResponse(responseCode = "403", description = "Forbidden"),
          @ApiResponse(responseCode = "404", description = "Not found"),
          @ApiResponse(responseCode = "500", description = "Internal error")
      })
  public ResponseEntity<LambdasResponse> getAllLambdas(@PathVariable Integer tenantId) {
    return ResponseEntity.ok(lambdaService.getAllLambdas(tenantId));
  }


  @GetMapping("/execution-time/{filter}/{tenantId}")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Successful request"),
          @ApiResponse(responseCode = "403", description = "Forbidden"),
          @ApiResponse(responseCode = "404", description = "Not found"),
          @ApiResponse(responseCode = "500", description = "Internal error")
      })
  public ResponseEntity<List<LambdaExecutionTimeResponse>> getExecutionTimes(
      @PathVariable String filter, @PathVariable Integer tenantId) {
    return ResponseEntity.ok(lambdaService.getExecutionTimes(
        filter, tenantId));
  }
  @GetMapping("/active-execution-time/{filter}/{tenantId}")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Successful request"),
          @ApiResponse(responseCode = "403", description = "Forbidden"),
          @ApiResponse(responseCode = "404", description = "Not found"),
          @ApiResponse(responseCode = "500", description = "Internal error")
      })
  public ResponseEntity<List<LambdaExecutionTimeResponse>> getExecutionActiveTimes(
      @PathVariable String filter, @PathVariable Integer tenantId) {
    return ResponseEntity.ok(lambdaService.getExecutionActiveTimes(
        filter, tenantId));
  }
  @PutMapping("/change-folder")
  public ResponseEntity<Void> changeFolder(@RequestBody ChangeLambdaFolderRequest changeLambdaFolderRequest)
  {
    lambdaService.changeFolder(changeLambdaFolderRequest);
    return ResponseEntity.ok().build();
  }

}
