package co.vuckovic.pegasus.common;

import co.vuckovic.pegasus.service.JwtUserDetailsService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

public class BaseUnitTest {

  @MockBean
  private JwtUserDetailsService jwtUserDetailsService;

  @MockBean
  protected PasswordEncoder passwordEncoder;

}
