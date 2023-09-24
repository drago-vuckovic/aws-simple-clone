package co.vuckovic.pegasus.repository;

import co.vuckovic.pegasus.repository.entity.UserEntity;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserEntityRepository extends JpaRepository<UserEntity, Integer> {

  Optional<UserEntity> findByEmail(String email);

  Optional<UserEntity> findByIdAndVerificationCode(Integer id, String verificationCode);

  Optional<UserEntity> findByVerificationCode(String verificationCode);

  Optional<List<UserEntity>> findByTenantId(Integer tenantId);

  boolean existsByEmail(String email);
}
