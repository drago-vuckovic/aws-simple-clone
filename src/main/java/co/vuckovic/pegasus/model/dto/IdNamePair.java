package co.vuckovic.pegasus.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdNamePair {

  private Integer fileId;
  private String fileName;
}
