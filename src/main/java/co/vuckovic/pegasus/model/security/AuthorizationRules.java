package co.vuckovic.pegasus.model.security;

import java.util.List;
import lombok.Data;

@Data
public class AuthorizationRules {

  List<Rule> rules;
}
