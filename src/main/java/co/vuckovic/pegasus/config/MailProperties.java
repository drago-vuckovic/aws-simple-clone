package co.vuckovic.pegasus.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.mail")
public class MailProperties {

  private String host;
  private Integer port;
  private String username;
  private String password;
  private String errorMessage;
  private String confirmationSubject;
  private String invitationSubject;
  private String recoverySubject;
  private String logErrorTemplate;
  private String transportProtocol;
  private Boolean smtpAuth;
  private Boolean smtpStarttlsEnable;

}
