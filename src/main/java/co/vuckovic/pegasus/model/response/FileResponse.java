package co.vuckovic.pegasus.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileResponse {

  private String name;
  private double size;
  private String type;
  private String filePath;
  private byte[] data;
}
