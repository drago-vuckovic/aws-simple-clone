package co.vuckovic.pegasus.model.request;

import co.vuckovic.pegasus.model.enumeration.TriggerType;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LambdaCheckRequest {
  @NotBlank
  private String srcDirPath;
  @NotBlank
  private TriggerType triggerType;
}
