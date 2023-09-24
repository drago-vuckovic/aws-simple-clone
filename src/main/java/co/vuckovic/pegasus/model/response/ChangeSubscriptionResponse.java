package co.vuckovic.pegasus.model.response;

import co.vuckovic.pegasus.model.enumeration.SubscriptionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeSubscriptionResponse {

  private SubscriptionType subscriptionType;
}
