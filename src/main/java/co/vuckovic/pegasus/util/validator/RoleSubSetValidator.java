package co.vuckovic.pegasus.util.validator;

import co.vuckovic.pegasus.util.constraint.RoleSubset;
import co.vuckovic.pegasus.model.enumeration.Role;

import java.util.Arrays;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class RoleSubSetValidator implements ConstraintValidator<RoleSubset, Role> {

  private Role[] subset;

  @Override
  public void initialize(RoleSubset constraintAnnotation) {
    this.subset = constraintAnnotation.anyOf();
  }

  @Override
  public boolean isValid(Role role, ConstraintValidatorContext constraintValidatorContext) {
    return role == null || Arrays.asList(subset).contains(role);
  }
}
