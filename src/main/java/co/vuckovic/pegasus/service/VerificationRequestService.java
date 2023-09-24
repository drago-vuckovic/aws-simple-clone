package co.vuckovic.pegasus.service;

import co.vuckovic.pegasus.repository.TenantEntityRepository;
import co.vuckovic.pegasus.model.enumeration.TenantStatus;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerificationRequestService {

  private final TenantEntityRepository tenantEntityRepository;

  @Scheduled(cron = "0 0/30 * * * ?")
  private void verificationTask() {

    tenantEntityRepository.deleteAllByStatusAndTimestampBefore(
        TenantStatus.UNVERIFIED, Timestamp.valueOf(LocalDateTime.now().minusMinutes(30)));
  }
}
