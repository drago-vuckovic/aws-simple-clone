package co.vuckovic.pegasus.model.request;

import co.vuckovic.pegasus.model.enumeration.PermissionType;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateFolderRequest {

  @NotNull
  private String name;
  private Integer parentId;
  @NotNull
  private Integer bucketId;
  @NotNull
  private PermissionType permissionTypeAdmin;
  @NotNull
  private PermissionType permissionTypeUser;
}
