package co.vuckovic.pegasus.repository;

import co.vuckovic.pegasus.repository.entity.InvitationEntity;
import co.vuckovic.pegasus.model.enumeration.InvitationStatus;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationEntityRepository extends JpaRepository<InvitationEntity, Integer> {

  Optional<List<InvitationEntity>> findInvitationEntitiesByTenantIdAndStatusLessThan(Integer tenant_id,InvitationStatus invitationStatus);

  Optional<InvitationEntity> findByIdAndVerificationCode(Integer id, String verificationCode);

  List<InvitationEntity> getAllByEmail(String email);

  boolean existsByEmailAndTenantIdAndStatus(
      String email, Integer tenantId, InvitationStatus invitationStatus);


}
