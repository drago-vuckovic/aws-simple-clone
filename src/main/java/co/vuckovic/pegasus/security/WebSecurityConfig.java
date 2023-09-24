package co.vuckovic.pegasus.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import co.vuckovic.pegasus.model.security.AuthorizationRules;
import co.vuckovic.pegasus.model.security.Rule;
import co.vuckovic.pegasus.service.JwtUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  private final AuthorizationFilter authorizationFilter;
  private final JwtUserDetailsService jwtUserDetailsService;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Bean
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

  @Override
  protected void configure(AuthenticationManagerBuilder authenticationManagerBuilder)
      throws Exception {
    authenticationManagerBuilder
        .userDetailsService(jwtUserDetailsService)
        .passwordEncoder(passwordEncoder);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http = http.cors().and().csrf().disable();

    http = http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and();

    AuthorizationRules authorizationRules =
        new ObjectMapper()
            .readValue(
                new ClassPathResource("rules.json").getInputStream(), AuthorizationRules.class);
    ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry interceptor =
        http.authorizeRequests();

    interceptor =
        interceptor
            .antMatchers(HttpMethod.POST, "/api/auth/sign-up")
            .permitAll()
            .antMatchers(HttpMethod.POST, "/api/auth/refresh-token")
            .permitAll()
            .antMatchers(HttpMethod.POST, "/api/auth/verify-email")
            .permitAll()
            .antMatchers(HttpMethod.POST, "/api/auth/forgotten-password")
            .permitAll()
            .antMatchers(HttpMethod.POST, "/api/auth/reset-password")
            .permitAll()
            .antMatchers(HttpMethod.POST, "/api/auth/login")
            .permitAll()
            .antMatchers(HttpMethod.POST, "/api/auth/resend-email")
            .permitAll()
            .antMatchers(HttpMethod.POST, "/api/invitations/accept-invite")
            .permitAll()
            .antMatchers(HttpMethod.GET, "/swagger-ui/*")
            .permitAll()
            .antMatchers(HttpMethod.GET, "/api/docs")
            .permitAll()
            .antMatchers(HttpMethod.GET, "/api/docs/*")
            .permitAll();

    for (Rule rule : authorizationRules.getRules()) {
      if (rule.getMethods().isEmpty()) {
        interceptor =
            interceptor
                .antMatchers(rule.getPattern())
                .hasAnyAuthority(rule.getRoles().toArray(String[]::new));
      } else {
        for (String method : rule.getMethods()) {
          interceptor =
              interceptor
                  .antMatchers(HttpMethod.resolve(method), rule.getPattern())
                  .hasAnyAuthority(rule.getRoles().toArray(String[]::new));
        }
      }
    }

    http = interceptor.anyRequest().denyAll().and();

    http.addFilterBefore(authorizationFilter, UsernamePasswordAuthenticationFilter.class);
  }
}
