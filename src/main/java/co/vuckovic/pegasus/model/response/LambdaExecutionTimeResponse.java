package co.vuckovic.pegasus.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LambdaExecutionTimeResponse {

  private String lambdaName;
  private Double duration;
}
