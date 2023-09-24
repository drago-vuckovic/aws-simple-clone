package co.vuckovic.pegasus.repository.entity;

import co.vuckovic.pegasus.model.enumeration.SubscriptionType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "Subscription_Package", schema = "public")
public class SubscriptionPackageEntity {

  @Id @GeneratedValue
  private Integer id;

  private String description;

  @Enumerated(EnumType.ORDINAL)
  private SubscriptionType subscriptionType;

  private Double capacity;

  private Integer maxNumOfLambdas;

}
