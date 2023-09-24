package co.vuckovic.pegasus.repository.entity;

import co.vuckovic.pegasus.model.enumeration.PermissionType;
import co.vuckovic.pegasus.model.enumeration.UserGroup;
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
@Table(name = "Permission", schema = "public")
public class PermissionEntity {

  @Id
  @GeneratedValue
  private Integer id;
  @Enumerated(EnumType.ORDINAL)
  private UserGroup userGroup;
  @Enumerated(EnumType.ORDINAL)
  private PermissionType permissionType;
  private Integer fileId;
}
