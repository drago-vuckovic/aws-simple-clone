package co.vuckovic.pegasus.model.request;

import co.vuckovic.pegasus.model.enumeration.PermissionType;
import co.vuckovic.pegasus.model.enumeration.UserGroup;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
public class FolderGroupPermissionChangeRequest {

  private final UserGroup userGroup;
  private final PermissionType permissionType;

}
