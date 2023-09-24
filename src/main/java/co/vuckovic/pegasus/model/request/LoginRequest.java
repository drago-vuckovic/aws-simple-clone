package co.vuckovic.pegasus.model.request;

import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
public class LoginRequest {

  @NotBlank
  private final String email;
  @NotBlank
  private final String password;
}
