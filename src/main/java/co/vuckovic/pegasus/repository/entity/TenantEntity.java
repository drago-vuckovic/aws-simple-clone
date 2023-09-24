package co.vuckovic.pegasus.repository.entity;

import co.vuckovic.pegasus.model.enumeration.TenantStatus;
import java.sql.Timestamp;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "Tenant", schema = "public")
public class TenantEntity {

  @Id
  @GeneratedValue
  private Integer id;

  @Column(unique = true)
  private String company;

  @Column(unique = true)
  private String subdomain;

  @Enumerated(EnumType.ORDINAL)
  private TenantStatus status;

  private Timestamp timestamp;
  @OneToMany(mappedBy = "tenantId")
  private List<UserEntity> users;
  @OneToMany(mappedBy = "tenantId")
  private List<InvitationEntity> invitations;

  @ManyToOne
  @JoinColumn(name = "subscription_package_id")
  private SubscriptionPackageEntity subscriptionPackageEntity;

  private Integer totalNumOfLambdas;
}
