package co.vuckovic.pegasus.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DummyPair {

  private Integer tenantId;
  private String subscriptionType;
}
