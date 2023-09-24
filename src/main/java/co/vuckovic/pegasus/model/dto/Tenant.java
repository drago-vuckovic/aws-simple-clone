package co.vuckovic.pegasus.model.dto;

import co.vuckovic.pegasus.model.enumeration.TenantStatus;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tenant {

  private Integer id;
  private String company;
  private String subdomain;
  private TenantStatus status;
  private Timestamp timestamp;
}
