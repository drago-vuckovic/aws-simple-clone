package co.vuckovic.pegasus.model.dto;

import java.sql.Timestamp;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class File {

  private Integer id;
  private String name;
  private Boolean isFolder;
  private Timestamp lastModified;
  private Double size;
  private Integer parentId;
  private String ownerEmail;
  private List<Permission> permissions;
}
