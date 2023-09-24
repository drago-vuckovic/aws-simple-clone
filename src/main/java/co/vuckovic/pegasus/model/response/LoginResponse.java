package co.vuckovic.pegasus.model.response;

import co.vuckovic.pegasus.model.enumeration.Role;
import co.vuckovic.pegasus.model.enumeration.SubscriptionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

  private Integer id;
  private String firstname;
  private String lastname;
  private String email;
  private Role role;
  private String token;
  private String refreshToken;
  private Integer tenantId;
  private String company;
  private Integer bucketId;
  private SubscriptionType subscriptionType;
}
