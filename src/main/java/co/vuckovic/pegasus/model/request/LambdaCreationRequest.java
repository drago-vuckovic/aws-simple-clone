package co.vuckovic.pegasus.model.request;

import co.vuckovic.pegasus.model.enumeration.LambdaLang;
import co.vuckovic.pegasus.model.enumeration.TriggerType;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LambdaCreationRequest {

  @NotBlank
  private String name;
  @NotBlank
  private Integer srcFolderId;
  @NotBlank
  private Integer destFolderId;
  @NotBlank
  private TriggerType triggerType;
  @NotBlank
  private LambdaLang lambdaLang;
  @NotBlank
  private String description;
  @NotBlank
  private Integer tenantId;
}
