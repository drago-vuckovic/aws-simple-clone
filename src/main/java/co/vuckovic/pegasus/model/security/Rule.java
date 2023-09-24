package co.vuckovic.pegasus.model.security;

import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Rule {

  private List<String> methods;
  private String pattern;
  private List<String> roles;
}
