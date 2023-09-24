package co.vuckovic.pegasus.repository;

import co.vuckovic.pegasus.repository.entity.SubscriptionPackageEntity;
import co.vuckovic.pegasus.model.enumeration.SubscriptionType;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionPackageEntityRepository extends
    JpaRepository<SubscriptionPackageEntity, Integer> {

  Optional<SubscriptionPackageEntity> findBySubscriptionType(SubscriptionType subscriptionType);
}
