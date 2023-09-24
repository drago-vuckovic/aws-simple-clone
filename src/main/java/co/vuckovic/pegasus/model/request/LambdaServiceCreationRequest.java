package co.vuckovic.pegasus.model.request;

import co.vuckovic.pegasus.model.enumeration.LambdaLang;
import co.vuckovic.pegasus.model.enumeration.TriggerType;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class LambdaServiceCreationRequest {

  private String name;
  private String srcPath;
  private String destPath;
  private TriggerType triggerType;
  private LambdaLang lambdaLang;
  private String createdBy;
  private Timestamp creationTime;
  private String description;
  private Integer tenantId;
}
