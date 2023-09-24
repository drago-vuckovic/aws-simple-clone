package co.vuckovic.pegasus.repository.entity;

import java.sql.Timestamp;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "File", schema = "public")
public class FileEntity {

  @Id
  @GeneratedValue
  private Integer id;
  private String name;
  private Boolean isFolder;
  private Timestamp lastModified;
  private Double size;
  private Integer parentId;
  private Integer bucketId;
  @ManyToOne
  @JoinColumn(name = "owner_id")
  private UserEntity owner;
  private String path;
  @OneToMany(mappedBy = "parentId")
  private List<FileEntity> children;
  @OneToMany(mappedBy = "fileId")
  private List<PermissionEntity> permissions;
}
