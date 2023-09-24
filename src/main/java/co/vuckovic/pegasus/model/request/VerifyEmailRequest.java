package co.vuckovic.pegasus.model.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class VerifyEmailRequest {

  @NotNull
  private final Integer id;
  @NotBlank
  private final String verificationCode;
  @NotBlank
  private final String company;
}
