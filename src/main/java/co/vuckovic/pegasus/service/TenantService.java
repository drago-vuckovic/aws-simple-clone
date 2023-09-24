package co.vuckovic.pegasus.service;

import co.vuckovic.pegasus.model.dto.JwtUser;
import co.vuckovic.pegasus.repository.BucketEntityRepository;
import co.vuckovic.pegasus.repository.SubscriptionPackageEntityRepository;
import co.vuckovic.pegasus.repository.TenantEntityRepository;
import co.vuckovic.pegasus.repository.UserEntityRepository;
import co.vuckovic.pegasus.repository.entity.BucketEntity;
import co.vuckovic.pegasus.repository.entity.SubscriptionPackageEntity;
import co.vuckovic.pegasus.repository.entity.TenantEntity;
import co.vuckovic.pegasus.repository.entity.UserEntity;
import co.vuckovic.pegasus.util.JwtUtil;
import co.vuckovic.pegasus.config.LambdaProperties;
import co.vuckovic.pegasus.model.enumeration.SubscriptionType;
import co.vuckovic.pegasus.model.exception.ActionNotAllowedException;
import co.vuckovic.pegasus.model.exception.NotFoundException;
import co.vuckovic.pegasus.model.response.ChangeSubscriptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class TenantService {

  private final TenantEntityRepository tenantEntityRepository;

  private final SubscriptionPackageEntityRepository subscriptionPackageEntityRepository;

  private final UserEntityRepository userEntityRepository;

  private final BucketEntityRepository bucketEntityRepository;

  private final JwtUtil jwtUtil;

  private final RestTemplate restTemplate;

  private final LambdaProperties lambdaProperties;

  public ChangeSubscriptionResponse changeSubscriptionPackage(Integer tenantId,
      String subscriptionType) {
    JwtUser currentUser = jwtUtil.getCurrentUser();

    UserEntity userEntity = userEntityRepository.getById(currentUser.getId());

    if (!userEntity.getTenantId().equals(tenantId)) {
      throw new ActionNotAllowedException("Only tenant owner can change subscription package");
    }

    TenantEntity tenantEntity = tenantEntityRepository.findById(tenantId)
        .orElseThrow(() -> new NotFoundException("Tenant doesn't exist"));
    SubscriptionPackageEntity newPackage = subscriptionPackageEntityRepository.findBySubscriptionType(
            SubscriptionType.valueOf(subscriptionType))
        .orElseThrow(() -> new NotFoundException("Subscription package doesn't exist"));

    if (newPackage.getSubscriptionType()
        .compareTo(tenantEntity.getSubscriptionPackageEntity().getSubscriptionType()) <= 0) {
      throw new ActionNotAllowedException(
          "Subscription package can only be upgraded, not downgraded");
    }

    tenantEntity.setSubscriptionPackageEntity(newPackage);
    tenantEntityRepository.save(tenantEntity);

    BucketEntity bucketEntity = bucketEntityRepository.findByTenantId(tenantId)
        .orElseThrow(() -> new NotFoundException("Bucket doesn't exist"));
    bucketEntity.setCapacity(newPackage.getCapacity());
    bucketEntityRepository.save(bucketEntity);

    try {
      restTemplate.put(
          String.format(
              "%s/set-unlimited-duration/%s",
              lambdaProperties.getBaseUrl(), tenantEntity.getId()),
          null,
          Integer.class);

    } catch (HttpStatusCodeException e) {
      throw new NotFoundException("Can't change max execution times");
    }

    return new ChangeSubscriptionResponse(newPackage.getSubscriptionType());
  }

}
