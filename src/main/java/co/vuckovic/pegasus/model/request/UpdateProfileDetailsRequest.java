package co.vuckovic.pegasus.model.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
public class UpdateProfileDetailsRequest {

  @Pattern(regexp = "^[a-zA-Z][a-zA-Z ]{0,28}[a-zA-Z]$")
  @NotBlank
  private final String firstname;

  @Pattern(regexp = "^[a-zA-Z][a-zA-Z ]{0,28}[a-zA-Z]$")
  @NotBlank
  private final String lastname;
}
