package co.vuckovic.pegasus.repository.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
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
@Table(name = "Statistic", schema = "public")
public class StatisticEntity {

  @Id
  @GeneratedValue
  private Integer id;
  private Integer numOfDownloads;
  private Integer numOfUploads;
  private Integer numOfFiles;
}
