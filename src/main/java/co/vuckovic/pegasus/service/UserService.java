package co.vuckovic.pegasus.service;

import co.vuckovic.pegasus.model.dto.JwtUser;
import co.vuckovic.pegasus.model.dto.User;
import co.vuckovic.pegasus.repository.BucketEntityRepository;
import co.vuckovic.pegasus.repository.TenantEntityRepository;
import co.vuckovic.pegasus.repository.UserEntityRepository;
import co.vuckovic.pegasus.repository.entity.BucketEntity;
import co.vuckovic.pegasus.repository.entity.TenantEntity;
import co.vuckovic.pegasus.repository.entity.UserEntity;
import co.vuckovic.pegasus.util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import co.vuckovic.pegasus.model.enumeration.Role;
import co.vuckovic.pegasus.model.exception.ActionNotAllowedException;
import co.vuckovic.pegasus.model.exception.ConflictException;
import co.vuckovic.pegasus.model.exception.InvalidRoleException;
import co.vuckovic.pegasus.model.exception.NotFoundException;
import co.vuckovic.pegasus.model.request.ChangePasswordRequest;
import co.vuckovic.pegasus.model.request.UpdateCompanyNameRequest;
import co.vuckovic.pegasus.model.request.UpdateProfileDetailsRequest;
import co.vuckovic.pegasus.model.response.BucketInfoResponse;
import co.vuckovic.pegasus.model.response.LoggedInUserResponse;
import co.vuckovic.pegasus.model.response.UserInfoResponse;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

  private final UserEntityRepository userEntityRepository;

  private final TenantEntityRepository tenantEntityRepository;

  private final ObjectMapper objectMapper;

  private final ModelMapper modelMapper;

  private final BucketEntityRepository bucketEntityRepository;

  private final PasswordEncoder passwordEncoder;

  private final JwtUtil jwtUtil;

  private final FileService fileService;


  public List<User> getAllUsersByTenantId(Integer tenantId) {
    return userEntityRepository.findByTenantId(tenantId).orElse(Collections.emptyList()).stream()
        .map(u -> modelMapper.map(u, User.class))
        .toList();
  }

  public LoggedInUserResponse getCurrentlyLoggedInUserByMail(String mail) {
    LoggedInUserResponse response =
        modelMapper.map(userEntityRepository.findByEmail(mail), LoggedInUserResponse.class);
    TenantEntity tenantEntity = tenantEntityRepository.getById(response.getTenantId());
    response.setCompany(tenantEntity.getCompany());
    response.setSubscriptionType(tenantEntity.getSubscriptionPackageEntity().getSubscriptionType());
    response.setBucketId(
        bucketEntityRepository
            .findBucketEntityByTenantId(
                userEntityRepository
                    .findByEmail(mail)
                    .orElseThrow(() -> new NotFoundException("Tenant not found."))
                    .getTenantId())
            .orElseThrow(() -> new NotFoundException("Bucket not found."))
            .getId());

    return response;
  }

  public void changeUserStatus(Integer id, JsonPatch userJsonPatch)
      throws JsonPatchException, JsonProcessingException {
    UserEntity userEntity =
        userEntityRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("User not found."));

    userEntityRepository.save(applyPatchToUser(userJsonPatch, userEntity));
  }

  public void changeUserRole(Integer id, JsonPatch userJsonPatch)
      throws JsonPatchException, JsonProcessingException {
    UserEntity userEntityBeforePatchApply =
        userEntityRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("User not found."));

    UserEntity userEntityAfterPatchApply =
        applyPatchToUser(userJsonPatch, userEntityBeforePatchApply);
    if (userEntityBeforePatchApply.getRole().equals(Role.OWNER)
        && !userEntityAfterPatchApply.getRole().equals(Role.OWNER)) {
      throw new InvalidRoleException("Owner's role cannot be downgraded.");
    }
    if (userEntityAfterPatchApply.getRole().equals(Role.OWNER)) {
      throw new InvalidRoleException("Role cannot be changed to Owner.");
    }
    userEntityRepository.save(userEntityAfterPatchApply);
  }

  public void updateProfileDetails(Integer userId, UpdateProfileDetailsRequest request) {

    if (!Objects.equals(jwtUtil.getCurrentUser().getId(), userId)) {
      throw new ActionNotAllowedException("You can't change other user's first or last name.");
    }

    UserEntity userEntity =
        userEntityRepository
            .findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found."));
    userEntity.setFirstname(request.getFirstname());
    userEntity.setLastname(request.getLastname());
    userEntityRepository.save(userEntity);
  }

  public void updateCompanyName(Integer id, UpdateCompanyNameRequest request) {
    if (!jwtUtil.getCurrentUser().getRole().equals(Role.OWNER)) {
      throw new ActionNotAllowedException("Only owner can change company name");
    }

    if (tenantEntityRepository.existsByCompany(request.getCompany())) {
      throw new ConflictException("This company name is already in use");
    }

    UserEntity userEntity =
        userEntityRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("User not found."));

    TenantEntity tenantEntity = tenantEntityRepository.findById(userEntity.getTenantId())
        .orElseThrow(() -> new NotFoundException("Tenant not found!"));
    tenantEntity.setCompany(request.getCompany());
    tenantEntityRepository.save(tenantEntity);

    BucketEntity bucketEntity = bucketEntityRepository.findBucketEntityByTenantId(
        tenantEntity.getId()).orElseThrow(() -> new NotFoundException("Bucket not found!"));

    fileService.updateBucketName(bucketEntity, request);
  }

  public void changePassword(Integer id, ChangePasswordRequest changePasswordRequest) {
    UserEntity userEntity = userEntityRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("User doesn't exist"));

    if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(),
        userEntity.getPassword())) {
      throw new ActionNotAllowedException("Old password is not valid");
    }

    if (changePasswordRequest.getNewPassword().equals(changePasswordRequest.getOldPassword())) {
      throw new ActionNotAllowedException("New password can't be same as current");
    }

    userEntity.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
    userEntityRepository.save(userEntity);
  }


  private UserEntity applyPatchToUser(JsonPatch userJsonPatch, UserEntity targetUser)
      throws JsonPatchException, JsonProcessingException {
    JsonNode patched = userJsonPatch.apply(objectMapper.convertValue(targetUser, JsonNode.class));
    return objectMapper.treeToValue(patched, UserEntity.class);
  }

  public UserInfoResponse getUserInfo() {
    JwtUser currentUser = jwtUtil.getCurrentUser();
    UserEntity userEntity = userEntityRepository.getById(currentUser.getId());
    return modelMapper.map(userEntity, UserInfoResponse.class);
  }

  public BucketInfoResponse getBucketInfo() {
    JwtUser currentUser = jwtUtil.getCurrentUser();
    UserEntity userEntity = userEntityRepository.getById(currentUser.getId());
    BucketEntity bucketEntity = bucketEntityRepository.findByTenantId(userEntity.getTenantId())
        .orElseThrow(() -> new NotFoundException("Bucket not found"));
    return modelMapper.map(bucketEntity, BucketInfoResponse.class);
  }
}
