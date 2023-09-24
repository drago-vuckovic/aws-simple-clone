package co.vuckovic.pegasus.util;

import co.vuckovic.pegasus.model.dto.JwtUser;
import co.vuckovic.pegasus.repository.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;

public class JwtUtil {

  @Value("${authorization.token.expiration-time}")
  private String tokenExpirationTime;

  @Value("${authorization.token.secret}")
  private String tokenSecret;

  @Value("${authorization.refresh-token.expiration-time}")
  private String refreshTokenExpirationTime;

  public String generateJwt(UserEntity user) {
    return Jwts.builder()
        .setId(user.getId().toString())
        .setSubject(user.getEmail())
        .claim("role", user.getRole().name())
        .claim("enabled", user.getEnabled())
        .setExpiration(new Date(System.currentTimeMillis() + Long.parseLong(tokenExpirationTime)))
        .signWith(SignatureAlgorithm.HS512, tokenSecret)
        .compact();
  }

  public String generateRefresh(UserEntity user) {
    return Jwts.builder()
        .setSubject(user.getEmail())
        .setExpiration(
            new Date(System.currentTimeMillis() + Long.parseLong(refreshTokenExpirationTime)))
        .signWith(SignatureAlgorithm.HS512, tokenSecret)
        .compact();
  }

  public Claims parseJwt(String token) {
    return Jwts.parser().setSigningKey(tokenSecret).parseClaimsJws(token).getBody();
  }

  public JwtUser getCurrentUser() {
    return (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }
}
