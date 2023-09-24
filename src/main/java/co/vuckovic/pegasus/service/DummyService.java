package co.vuckovic.pegasus.service;

import co.vuckovic.pegasus.model.dto.DummyPair;
import co.vuckovic.pegasus.repository.TenantEntityRepository;
import co.vuckovic.pegasus.config.LambdaProperties;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class DummyService {

  private final TenantEntityRepository tenantEntityRepository;

  private final RestTemplate restTemplate;

  private final LambdaProperties lambdaProperties;

  public void populateMicroserviceDB() {
    List<DummyPair> pairs = tenantEntityRepository.findAll().stream()
        .map(entity -> new DummyPair(entity.getId(), entity.getSubscriptionPackageEntity().getSubscriptionType().toString())).toList();

    restTemplate.postForObject(
        lambdaProperties.getDummyUrl(),
        pairs,
        ResponseEntity.class);
  }
}
