package co.vuckovic.pegasus.model.dto;

import co.vuckovic.pegasus.model.enumeration.PermissionType;
import co.vuckovic.pegasus.model.enumeration.UserGroup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

  private UserGroup userGroup;
  private PermissionType permissionType;
}
