package co.vuckovic.pegasus.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Statistic {

  private Integer id;
  private Integer numOfDownloads;
  private Integer numOfUploads;
}
