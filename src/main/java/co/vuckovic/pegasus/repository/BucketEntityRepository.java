package co.vuckovic.pegasus.repository;

import co.vuckovic.pegasus.repository.entity.BucketEntity;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BucketEntityRepository extends JpaRepository<BucketEntity, Integer> {

  Optional<BucketEntity> findByTenantId(Integer tenantId);
  Optional<BucketEntity> findBucketEntityByTenantId(Integer tenant_id);
}
