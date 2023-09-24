package co.vuckovic.pegasus.model.dto;

import co.vuckovic.pegasus.model.enumeration.SubscriptionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPackage {

  private Integer id;

  private String description;

  private SubscriptionType subscriptionType;

  private Double capacity;
}
