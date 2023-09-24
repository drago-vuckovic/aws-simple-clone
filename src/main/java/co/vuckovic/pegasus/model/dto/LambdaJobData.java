package co.vuckovic.pegasus.model.dto;

import co.vuckovic.pegasus.model.enumeration.TriggerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LambdaJobData {

  private String srcPath;
  private TriggerType triggerType;
  private String fileName;
  private byte[] fileBytes;
}
