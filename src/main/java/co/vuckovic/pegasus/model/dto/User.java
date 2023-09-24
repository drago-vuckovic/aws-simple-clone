package co.vuckovic.pegasus.model.dto;

import co.vuckovic.pegasus.model.enumeration.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

  private Integer id;
  private String firstname;
  private String lastname;
  private String email;
  private Role role;
  private String verificationCode;
  private Boolean enabled;
}
