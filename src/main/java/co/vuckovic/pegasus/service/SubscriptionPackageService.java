package co.vuckovic.pegasus.service;

import co.vuckovic.pegasus.model.dto.SubscriptionPackage;
import co.vuckovic.pegasus.model.dto.SubscriptionPackageList;
import co.vuckovic.pegasus.repository.SubscriptionPackageEntityRepository;
import co.vuckovic.pegasus.repository.entity.SubscriptionPackageEntity;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionPackageService {

  private final SubscriptionPackageEntityRepository subscriptionPackageEntityRepository;
  private final ModelMapper modelMapper;

  public List<SubscriptionPackage> getAllSubscriptionPackages() {
    List<SubscriptionPackageEntity> subscriptionEntities = subscriptionPackageEntityRepository.findAll();
    List<SubscriptionPackage> subscriptions;
    subscriptions = modelMapper.map(subscriptionEntities, SubscriptionPackageList.class);
    return subscriptions;
  }
}
