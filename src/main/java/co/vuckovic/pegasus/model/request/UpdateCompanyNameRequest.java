package co.vuckovic.pegasus.model.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCompanyNameRequest {

  @Pattern(regexp = "^[a-zA-Z1-9][a-zA-Z1-9 ]{0,28}[a-zA-Z1-9]$")
  @NotBlank
  private String company;
}
