package co.vuckovic.pegasus.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangeLambdaFolderRequest {

  private String oldPath;
  private String newPath;
}
