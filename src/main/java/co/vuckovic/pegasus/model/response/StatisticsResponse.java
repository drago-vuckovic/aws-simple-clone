package co.vuckovic.pegasus.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponse {

  private Integer numOfDownloads;
  private Integer numOfUploads;
  private Integer numOfFiles;
}
