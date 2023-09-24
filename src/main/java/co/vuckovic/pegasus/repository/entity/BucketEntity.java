package co.vuckovic.pegasus.repository.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
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
@Table(name = "Bucket", schema = "public")
public class BucketEntity {

  @Id
  @GeneratedValue
  private Integer id;
  private Double size;
  private Double capacity;
  private String name;
  private Integer tenantId;
  @OneToOne
  @JoinColumn(name = "statistic_id")
  private StatisticEntity statisticEntity;
}
