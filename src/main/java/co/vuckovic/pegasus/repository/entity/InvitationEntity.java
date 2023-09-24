package co.vuckovic.pegasus.repository.entity;

import co.vuckovic.pegasus.model.enumeration.InvitationStatus;
import co.vuckovic.pegasus.model.enumeration.Role;
import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Invitation", schema = "public")
public class InvitationEntity {

  @Id
  @GeneratedValue
  private Integer id;
  private String firstname;
  private String lastname;
  private String email;
  private Timestamp timestamp;
  @Enumerated(EnumType.ORDINAL)
  private Role role;
  @Enumerated(EnumType.ORDINAL)
  private InvitationStatus status;
  private String verificationCode;
  private Integer tenantId;
}
