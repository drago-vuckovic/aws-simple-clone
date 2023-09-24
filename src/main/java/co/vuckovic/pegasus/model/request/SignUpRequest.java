package co.vuckovic.pegasus.model.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
public class SignUpRequest {

  // This can be changed
  @Pattern(regexp = "^[a-zA-Z][a-zA-Z ]{0,28}[a-zA-Z]$")
  @NotBlank
  private final String firstname;

  @Pattern(regexp = "^[a-zA-Z][a-zA-Z ]{0,28}[a-zA-Z]$")
  @NotBlank
  private final String lastname;

  @NotBlank
  @Email(
      message = "Email is not valid",
      regexp =
          "^(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$")
  @NotEmpty(message = "Email cannot be empty")
  private final String email;

  @NotBlank
  @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,255}$")
  private final String password;

  @Pattern(regexp = "^[a-zA-Z1-9][a-zA-Z1-9 ]{0,28}[a-zA-Z1-9]$")
  @NotBlank
  private final String company;

  @Pattern(regexp = "([a-zA-Z 1-9]{2,30}.)+pegasus.com")
  @NotBlank
  private final String subdomain;
}
