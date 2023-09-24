package co.vuckovic.pegasus.repository.entity;

import co.vuckovic.pegasus.model.enumeration.Role;
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
@Table(name = "User", schema = "public")
public class UserEntity {

  @Id
  @GeneratedValue
  private Integer id;
  private String firstname;
  private String lastname;
  private String email;
  private String password;
  @Enumerated(EnumType.ORDINAL)
  private Role role;
  private String verificationCode;
  private Boolean enabled;
  private Integer tenantId;
}
