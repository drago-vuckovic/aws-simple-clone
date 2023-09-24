package co.vuckovic.pegasus.repository;

import co.vuckovic.pegasus.repository.entity.TenantEntity;
import co.vuckovic.pegasus.model.enumeration.TenantStatus;

import java.sql.Timestamp;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface TenantEntityRepository extends JpaRepository<TenantEntity, Integer> {

  boolean existsByCompanyOrSubdomain(String company, String subdomain);

  @Transactional
  @Modifying
  void deleteAllByStatusAndTimestampBefore(TenantStatus status, Timestamp calculatedValue);

  Optional<TenantEntity> findByIdAndCompany(Integer id, String company);

  boolean existsByCompany(String company);
}
