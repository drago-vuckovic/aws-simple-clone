package co.vuckovic.pegasus.model.response;

import co.vuckovic.pegasus.model.enumeration.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {

  private String firstname;
  private String lastname;
  private String email;
  private Role role;

}
