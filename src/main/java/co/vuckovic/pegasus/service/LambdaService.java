package co.vuckovic.pegasus.service;

import co.vuckovic.pegasus.model.dto.JwtUser;
import co.vuckovic.pegasus.model.dto.LambdaJobData;
import co.vuckovic.pegasus.repository.FileEntityRepository;
import co.vuckovic.pegasus.repository.TenantEntityRepository;
import co.vuckovic.pegasus.repository.UserEntityRepository;
import co.vuckovic.pegasus.repository.entity.FileEntity;
import co.vuckovic.pegasus.repository.entity.TenantEntity;
import co.vuckovic.pegasus.repository.entity.UserEntity;
import co.vuckovic.pegasus.util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import co.vuckovic.pegasus.config.LambdaProperties;
import co.vuckovic.pegasus.model.enumeration.TriggerType;
import co.vuckovic.pegasus.model.exception.ActionNotAllowedException;
import co.vuckovic.pegasus.model.exception.HttpException;
import co.vuckovic.pegasus.model.exception.NotFoundException;
import co.vuckovic.pegasus.model.request.ChangeLambdaFolderRequest;
import co.vuckovic.pegasus.model.request.LambdaCheckRequest;
import co.vuckovic.pegasus.model.request.LambdaCreationRequest;
import co.vuckovic.pegasus.model.request.LambdaServiceCreationRequest;
import co.vuckovic.pegasus.model.request.LambdaServiceUpdateRequest;
import co.vuckovic.pegasus.model.request.LambdaUpdateRequest;
import co.vuckovic.pegasus.model.request.LambdaUpdateStatusRequest;
import co.vuckovic.pegasus.model.response.LambdaExecutionTimeResponse;
import co.vuckovic.pegasus.model.response.LambdasResponse;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class LambdaService {

  private final UserEntityRepository userEntityRepository;

  private final TenantEntityRepository tenantEntityRepository;

  private final LambdaProperties lambdaProperties;

  private final ModelMapper modelMapper;

  private final RestTemplate restTemplate;

  private final FileEntityRepository fileEntityRepository;

  private final ObjectMapper objectMapper;

  private final KafkaTemplate<String, LambdaJobData> kafkaTemplate;

  private final JwtUtil jwtUtil;

  public void sendMessage(LambdaJobData lambdaJobData) {
    kafkaTemplate.send("lambda-topic", lambdaJobData);
  }

  public Integer createLambda(MultipartFile lambdaFile, LambdaCreationRequest lambdaCreationRequest)
      throws JsonProcessingException {

    UserEntity user = userEntityRepository.getById(jwtUtil.getCurrentUser().getId());
    TenantEntity tenantEntity = tenantEntityRepository.getById(lambdaCreationRequest.getTenantId());
    if (tenantEntity.getSubscriptionPackageEntity().getMaxNumOfLambdas() == null
        || tenantEntity.getTotalNumOfLambdas()
        < tenantEntity.getSubscriptionPackageEntity().getMaxNumOfLambdas()) {
      return sendRequestToLambdaService(lambdaFile, lambdaCreationRequest, user, tenantEntity);
    } else {
      throw new ActionNotAllowedException("You have reached maximum number of lambdas");
    }
  }

  public void updateLambda(
      Integer id, MultipartFile lambdaFile, LambdaUpdateRequest lambdaUpdateRequest)
      throws JsonProcessingException {

    JwtUser currentUser = jwtUtil.getCurrentUser();

    LambdaServiceUpdateRequest lambdaServiceUpdateRequest =
        modelMapper.map(lambdaUpdateRequest, LambdaServiceUpdateRequest.class);

    FileEntity srcFolder =
        fileEntityRepository
            .findById(lambdaUpdateRequest.getSrcFolderId())
            .orElseThrow(() -> new NotFoundException("Source folder not found"));
    FileEntity destFolder =
        fileEntityRepository
            .findById(lambdaUpdateRequest.getDestFolderId())
            .orElseThrow(() -> new NotFoundException("Destination folder not found"));

    lambdaServiceUpdateRequest.setSrcPath(srcFolder.getPath());
    lambdaServiceUpdateRequest.setDestPath(destFolder.getPath());
    lambdaServiceUpdateRequest.setCurrentUserEmail(currentUser.getEmail());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

    if (lambdaFile != null) {
      body.add("file", lambdaFile.getResource());
    }

    body.add("lambda", objectMapper.writeValueAsString(lambdaServiceUpdateRequest));

    HttpEntity<MultiValueMap<String, Object>> requestEntity;
    requestEntity = new HttpEntity<>(body, headers);
    Map<String, Integer> params = new HashMap<>();
    params.put("id", id);
    try {
      restTemplate.put(lambdaProperties.getBaseUrl() + "/{id}", requestEntity, params);
    } catch (HttpStatusCodeException e) {
      throw new HttpException("Lambda update failed");
    }
  }

  private Integer sendRequestToLambdaService(
      MultipartFile lambdaFile,
      LambdaCreationRequest lambdaCreationRequest,
      UserEntity user,
      TenantEntity tenantEntity)
      throws JsonProcessingException {

    LambdaServiceCreationRequest request =
        modelMapper.map(lambdaCreationRequest, LambdaServiceCreationRequest.class);

    FileEntity srcFolder =
        fileEntityRepository
            .findFileEntityByIdAndIsFolder(lambdaCreationRequest.getSrcFolderId(), true)
            .orElseThrow(() -> new NotFoundException("Source folder not found"));
    FileEntity destFolder =
        fileEntityRepository
            .findFileEntityByIdAndIsFolder(lambdaCreationRequest.getDestFolderId(), true)
            .orElseThrow(() -> new NotFoundException("Destination folder not found"));

    request.setSrcPath(srcFolder.getPath());
    request.setDestPath(destFolder.getPath());
    request.setCreationTime(Timestamp.from(Instant.now()));
    request.setCreatedBy(user.getEmail());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

    body.add("file", lambdaFile.getResource());
    body.add("lambda", objectMapper.writeValueAsString(request));

    HttpEntity<MultiValueMap<String, Object>> requestEntity;
    requestEntity = new HttpEntity<>(body, headers);

    try {
      ResponseEntity<Integer> response =
          restTemplate.postForEntity(
              lambdaProperties.getBaseUrl() + "/create", requestEntity, Integer.class);
      tenantEntity.setTotalNumOfLambdas(tenantEntity.getTotalNumOfLambdas() + 1);
      tenantEntityRepository.save(tenantEntity);

      return response.getBody();
    } catch (HttpStatusCodeException e) {
      e.printStackTrace();
      throw new HttpException("Lambda creation failed. Check if your file has compilation errors.");
    }
  }

  public boolean checkIfAnyLambdaExists(String srcDirPath, TriggerType triggerType) {

    LambdaCheckRequest lambdaCheckRequest =
        LambdaCheckRequest.builder().srcDirPath(srcDirPath).triggerType(triggerType).build();

    try {
      ResponseEntity<Boolean> response =
          restTemplate.postForEntity(
              lambdaProperties.getBaseUrl() + "/check-if-exists",
              lambdaCheckRequest,
              Boolean.class);
      return response.getBody();

    } catch (HttpStatusCodeException e) {
      throw new HttpException("Lambda creation failed. Check if your file has compilation errors.");
    }
  }

  public List<LambdaExecutionTimeResponse> getExecutionTimes(String filter, Integer tenantId) {
    if (!tenantEntityRepository.existsById(tenantId)) {
      throw new NotFoundException("Tenant doesn't exist");
    }
    try {
      return restTemplate.getForObject(
          String.format("%s/execution-time/%s/%s", lambdaProperties.getBaseUrl(), filter, tenantId),
          List.class);
    } catch (HttpStatusCodeException e) {
      throw new NotFoundException("Can't get execution times");
    }
  }

  public List<LambdaExecutionTimeResponse> getExecutionActiveTimes(
      String filter, Integer tenantId) {
    if (!tenantEntityRepository.existsById(tenantId)) {
      throw new NotFoundException("Tenant doesn't exist");
    }
    try {
      return restTemplate.getForObject(
          String.format("%s/active-execution-time/%s/%s", lambdaProperties.getBaseUrl(), filter,
              tenantId),
          List.class);
    } catch (HttpStatusCodeException e) {
      throw new NotFoundException("Can't get execution times");
    }
  }

  public LambdasResponse getAllLambdas(Integer tenantId) {
    ResponseEntity<LambdasResponse> response = null;
    try {
      response =
          restTemplate.getForEntity(
              String.format("%s/%s", lambdaProperties.getBaseUrl(), tenantId),
              LambdasResponse.class);
    } catch (HttpStatusCodeException e) {
      throw new HttpException("Lambda response failed.");
    }
    return response.getBody();
  }

  public void updateLambdaStatus(Integer id, LambdaUpdateStatusRequest lambdaUpdateRequest) {
    try {
      Map<String, Integer> params = new HashMap<>();
      params.put("id", id);
      restTemplate.put(lambdaProperties.getBaseUrl() + "/{id}/status", lambdaUpdateRequest, params);
    } catch (HttpStatusCodeException e) {
      throw new HttpException("Lambda update failed.");
    }
  }

  public void deleteLambda(Integer id) {
    try {
      Map<String, Integer> params = new HashMap<>();
      params.put("id", id);
      restTemplate.delete(lambdaProperties.getBaseUrl() + "/{id}", params);
    } catch (HttpStatusCodeException e) {
      throw new HttpException("Lambda delete failed.");
    }
    JwtUser user = jwtUtil.getCurrentUser();
    UserEntity userEntity =
        userEntityRepository
            .findById(user.getId())
            .orElseThrow(() -> new NotFoundException("User not found!"));
    TenantEntity tenantEntity =
        tenantEntityRepository
            .findById(userEntity.getTenantId())
            .orElseThrow(() -> new NotFoundException("Tenant not found!"));
    decrementTotalNumberOfLambdas(tenantEntity);
  }

  private void decrementTotalNumberOfLambdas(TenantEntity tenantEntity) {
    tenantEntity.setTotalNumOfLambdas(tenantEntity.getTotalNumOfLambdas() - 1);
    tenantEntityRepository.save(tenantEntity);
  }

  public void changeFolder(ChangeLambdaFolderRequest changeLambdaFolderRequest) {
    try {

      restTemplate.put(lambdaProperties.getBaseUrl() + "/change-folder", changeLambdaFolderRequest);
    } catch (HttpStatusCodeException e) {
      throw new HttpException("Change src and dest folder failed.");
    }
  }
}
