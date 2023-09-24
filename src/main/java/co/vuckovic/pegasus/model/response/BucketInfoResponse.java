package co.vuckovic.pegasus.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BucketInfoResponse {

  private double size;
  private double capacity;

}
