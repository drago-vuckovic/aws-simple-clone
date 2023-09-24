package co.vuckovic.pegasus.model.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LambdaExecutionTimeList {

  private List<LambdaExecutionTimeResponse> lambdaExecutionTimeResponseList;

}
