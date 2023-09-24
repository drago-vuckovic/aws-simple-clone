package co.vuckovic.pegasus.model.response;

import co.vuckovic.pegasus.model.enumeration.Role;
import co.vuckovic.pegasus.model.enumeration.SubscriptionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoggedInUserResponse {

  private Integer id;
  private String firstname;
  private String lastname;
  private String email;
  private Role role;
  private Boolean enabled;
  private Integer tenantId;
  private Integer bucketId;
  private String company;
  private SubscriptionType subscriptionType;
}
