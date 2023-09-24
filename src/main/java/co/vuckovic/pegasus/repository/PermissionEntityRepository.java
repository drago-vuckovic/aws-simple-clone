package co.vuckovic.pegasus.repository;

import co.vuckovic.pegasus.repository.entity.PermissionEntity;
import co.vuckovic.pegasus.model.enumeration.UserGroup;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionEntityRepository extends JpaRepository<PermissionEntity, Integer> {

  Optional<PermissionEntity> findByFileIdAndUserGroup(Integer fileId, UserGroup userGroup);
}
