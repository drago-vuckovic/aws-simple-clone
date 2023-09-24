package co.vuckovic.pegasus.model.request;

import co.vuckovic.pegasus.model.enumeration.Role;
import co.vuckovic.pegasus.util.constraint.RoleSubset;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InviteUserRequest {

  @NotBlank
  @Pattern(regexp = "^[a-zA-Z][a-zA-Z ]{0,28}[a-zA-Z]$")
  private String firstname;
  @NotBlank
  @Pattern(regexp = "^[a-zA-Z][a-zA-Z ]{0,28}[a-zA-Z]$")
  private String lastname;

  @NotBlank
  @Email(
      message = "Email is not valid",
      regexp =
          "^(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$")
  @NotEmpty(message = "Email cannot be empty")
  private String email;

  @NotNull
  @RoleSubset(anyOf = {Role.ADMIN, Role.USER})
  private Role role;
  @NotNull
  private Integer tenantId;
}
