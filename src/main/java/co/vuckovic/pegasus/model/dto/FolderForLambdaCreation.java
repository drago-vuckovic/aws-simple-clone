package co.vuckovic.pegasus.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderForLambdaCreation {

  private Integer id;
  private String name;
  private String path;
}
