package co.vuckovic.pegasus.model.dto;

import co.vuckovic.pegasus.model.enumeration.InvitationStatus;
import co.vuckovic.pegasus.model.enumeration.Role;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invitation {

  private Integer id;
  private String firstname;
  private String lastname;
  private String email;
  private Role role;
  private String verificationCode;
  private Timestamp timestamp;
  private Integer tenantId;
  private InvitationStatus status;
}
