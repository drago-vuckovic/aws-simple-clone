package co.vuckovic.pegasus.security;

import co.vuckovic.pegasus.model.dto.JwtUser;
import co.vuckovic.pegasus.model.enumeration.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
public class AuthorizationFilter extends OncePerRequestFilter {

  @Value("${authorization.token.header.name}")
  private String authorizationHeaderName;

  @Value("${authorization.token.header.prefix}")
  private String authorizationHeaderPrefix;

  @Value("${authorization.token.secret}")
  private String authorizationSecret;

  @Override
  protected void doFilterInternal(
      HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse,
      FilterChain filterChain)
      throws ServletException, IOException {
    String authorizationHeader = httpServletRequest.getHeader(authorizationHeaderName);
    if (authorizationHeader == null || !authorizationHeader.startsWith(authorizationHeaderPrefix)) {
      filterChain.doFilter(httpServletRequest, httpServletResponse);
      return;
    }
    String token = authorizationHeader.replace(authorizationHeaderPrefix, "");
    try {
      Claims claims =
          Jwts.parser().setSigningKey(authorizationSecret).parseClaimsJws(token).getBody();
      JwtUser jwtUser =
          new JwtUser(
              Integer.valueOf(claims.getId()),
              claims.getSubject(),
              null,
              Role.valueOf(claims.get("role", String.class)),
              claims.get("enabled", Boolean.class));
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(jwtUser, null, jwtUser.getAuthorities());
      SecurityContextHolder.getContext().setAuthentication(authentication);
      filterChain.doFilter(httpServletRequest, httpServletResponse);
    } catch (ExpiredJwtException e) {
      log.error(
          String.format("JWT Authentication failed from: %s", httpServletRequest.getRemoteHost()));
      httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
  }
}
