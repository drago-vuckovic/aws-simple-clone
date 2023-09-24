package co.vuckovic.pegasus.model.request;

import co.vuckovic.pegasus.model.enumeration.LambdaLang;
import co.vuckovic.pegasus.model.enumeration.TriggerType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LambdaUpdateRequest {

  private String name;
  private Integer srcFolderId;
  private Integer destFolderId;
  private TriggerType triggerType;
  private LambdaLang lambdaLang;
  private String description;
}
