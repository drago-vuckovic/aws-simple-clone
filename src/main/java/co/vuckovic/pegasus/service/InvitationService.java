package co.vuckovic.pegasus.service;

import co.vuckovic.pegasus.model.dto.Invitation;
import co.vuckovic.pegasus.model.dto.InvitationList;
import co.vuckovic.pegasus.model.dto.Tenant;
import co.vuckovic.pegasus.repository.BucketEntityRepository;
import co.vuckovic.pegasus.repository.InvitationEntityRepository;
import co.vuckovic.pegasus.repository.TenantEntityRepository;
import co.vuckovic.pegasus.repository.UserEntityRepository;
import co.vuckovic.pegasus.repository.entity.InvitationEntity;
import co.vuckovic.pegasus.repository.entity.TenantEntity;
import co.vuckovic.pegasus.repository.entity.UserEntity;
import co.vuckovic.pegasus.util.JwtUtil;
import co.vuckovic.pegasus.model.enumeration.InvitationStatus;
import co.vuckovic.pegasus.model.exception.ConflictException;
import co.vuckovic.pegasus.model.exception.NotFoundException;
import co.vuckovic.pegasus.model.request.AcceptInviteRequest;
import co.vuckovic.pegasus.model.request.InviteUserRequest;
import co.vuckovic.pegasus.model.response.InviteUserResponse;
import co.vuckovic.pegasus.model.response.LoginResponse;

import java.sql.Timestamp;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.utility.RandomString;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InvitationService {

  private final InvitationEntityRepository invitationEntityRepository;

  private final UserEntityRepository userEntityRepository;

  private final TenantEntityRepository tenantEntityRepository;

  private final ModelMapper modelMapper;

  private final MailService mailService;

  private final PasswordEncoder passwordEncoder;

  private final JwtUtil jwtUtil;

  private final BucketEntityRepository bucketEntityRepository;


  public InviteUserResponse createInvitation(InviteUserRequest inviteUserRequest) {
    if (!tenantEntityRepository.existsById(inviteUserRequest.getTenantId())) {
      throw new NotFoundException("Tenant doesn't exist");
    }

    if (userEntityRepository.existsByEmail(inviteUserRequest.getEmail())) {
      throw new ConflictException("User with this email is already member of one tenant");
    }

    if (invitationEntityRepository.existsByEmailAndTenantIdAndStatus(
        inviteUserRequest.getEmail(), inviteUserRequest.getTenantId(), InvitationStatus.PENDING)) {
      throw new ConflictException("User with this email is already invited to this tenant!");
    }

    InvitationEntity invitationEntity = modelMapper.map(inviteUserRequest, InvitationEntity.class);
    invitationEntity.setId(0);
    invitationEntity.setStatus(InvitationStatus.PENDING);
    invitationEntity = setVerificationCodeTimestampAndSendInvitationMail(invitationEntity);

    return new InviteUserResponse(invitationEntity.getId());
  }

  public LoginResponse acceptInvite(AcceptInviteRequest acceptInviteRequest) {
    InvitationEntity invitationEntity =
        invitationEntityRepository
            .findByIdAndVerificationCode(
                acceptInviteRequest.getInvitationId(), acceptInviteRequest.getVerificationCode())
            .orElseThrow(() -> new NotFoundException("Verification code is not valid!"));

    if (!invitationEntity.getStatus().equals(InvitationStatus.PENDING)) {
      throw new NotFoundException("Invitation can't be accepted anymore");
    }

    invitationEntity.setStatus(InvitationStatus.ACCEPTED);
    invitationEntity.setVerificationCode(null);
    invitationEntityRepository.save(invitationEntity);

    UserEntity userEntity = modelMapper.map(invitationEntity, UserEntity.class);
    userEntity.setId(0);
    userEntity.setPassword(passwordEncoder.encode(acceptInviteRequest.getPassword()));
    userEntity.setVerificationCode(null);
    userEntity.setEnabled(true);
    userEntity = userEntityRepository.save(userEntity);

    TenantEntity tenantEntity =
        tenantEntityRepository
            .findById(userEntity.getTenantId())
            .orElseThrow(NotFoundException::new);

    LoginResponse response = modelMapper.map(userEntity, LoginResponse.class);
    response.setToken(jwtUtil.generateJwt(userEntity));
    response.setRefreshToken(jwtUtil.generateRefresh(userEntity));
    response.setCompany(tenantEntity.getCompany());
    response.setBucketId(
        bucketEntityRepository
            .findBucketEntityByTenantId(tenantEntity.getId())
            .orElseThrow(() -> new NotFoundException("Bucket not found."))
            .getId());

    List<InvitationEntity> invitations =
        invitationEntityRepository.getAllByEmail(invitationEntity.getEmail());
    invitations.stream()
        .forEach(
            i -> {
              if (!i.getId().equals(acceptInviteRequest.getInvitationId())) {
                i.setStatus(InvitationStatus.REJECTED);
                i.setVerificationCode(null);
                invitationEntityRepository.save(i);
              }
            });

    return response;
  }

  public void resendInvite(Integer invitationId) {
    InvitationEntity invitationEntity =
        invitationEntityRepository
            .findById(invitationId)
            .orElseThrow(() -> new NotFoundException("Mail cannot be sent!"));
    if (invitationEntity.getStatus().equals(InvitationStatus.PENDING)) {
      setVerificationCodeTimestampAndSendInvitationMail(invitationEntity);
    } else {
      throw new NotFoundException("This invitation is not pending!");
    }
  }

  public void cancelInvitation(Integer invitationId) {
    InvitationEntity invitationEntity =
        invitationEntityRepository
            .findById(invitationId)
            .orElseThrow(() -> new NotFoundException("Invitation not found."));
    invitationEntity.setStatus(InvitationStatus.CANCELLED);
    invitationEntityRepository.save(invitationEntity);
  }

  public List<Invitation> getAllInvitationsByTenantId(Integer tenantId) {
    List<InvitationEntity> invitationEntities =
        invitationEntityRepository
            .findInvitationEntitiesByTenantIdAndStatusLessThan(tenantId, InvitationStatus.ACCEPTED)
            .orElseThrow(() -> new NotFoundException("There are no invited users!"));
    List<Invitation> invitations;
    invitations = modelMapper.map(invitationEntities, InvitationList.class);
    return invitations;
  }

  private InvitationEntity setVerificationCodeTimestampAndSendInvitationMail(
      InvitationEntity invitationEntity) {
    invitationEntity.setTimestamp(new Timestamp(System.currentTimeMillis()));
    String randomCode = RandomString.make(64);
    invitationEntity.setVerificationCode(randomCode);
    invitationEntity = invitationEntityRepository.save(invitationEntity);
    TenantEntity tenantEntity = tenantEntityRepository.getById(invitationEntity.getTenantId());
    mailService.sendInvitationMail(
        modelMapper.map(invitationEntity, Invitation.class),
        modelMapper.map(tenantEntity, Tenant.class));

    return invitationEntity;
  }
}
