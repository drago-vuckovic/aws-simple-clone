package co.vuckovic.pegasus.config;

import co.vuckovic.pegasus.util.JwtUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.modelmapper.ModelMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Data
@RequiredArgsConstructor
public class BeanInitializer {

  private final MailProperties mailProperties;

  private final FrontendProperties frontendProperties;

  @Bean
  public JavaMailSender getJavaMailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(mailProperties.getHost());
    mailSender.setPort(mailProperties.getPort());

    mailSender.setUsername(mailProperties.getUsername());
    mailSender.setPassword(mailProperties.getPassword());

    Properties props = mailSender.getJavaMailProperties();
    props.put("mail.transport.protocol", mailProperties.getTransportProtocol());
    props.put("mail.smtp.auth", mailProperties.getSmtpAuth());
    props.put("mail.smtp.starttls.enable", mailProperties.getSmtpStarttlsEnable());

    return mailSender;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public ModelMapper modelMapper() {
    ModelMapper mapper = new ModelMapper();
    mapper.getConfiguration().setAmbiguityIgnored(true);
    return mapper;
  }

  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry
            .addMapping("/**")
            .allowedOrigins(frontendProperties.getBaseUrl())
            .allowedHeaders("*")
            .allowedMethods("*");
      }
    };
  }

  @Bean
  public JwtUtil jwtUtil() {
    return new JwtUtil();
  }

  @Bean
  GrantedAuthorityDefaults grantedAuthorityDefaults() {
    return new GrantedAuthorityDefaults("");
  }

  @Bean
  public NewTopic lambdaTopic() {
    NewTopic newTopic = TopicBuilder.name("lambda-topic").build();
    Map<String, String> configs = new HashMap<>();
    configs.put("max.message.bytes", "33554432");
    configs.put("max.request.size", "33554432");
    newTopic.configs(configs);
    return newTopic;
  }


  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    // Do any additional configuration here
    return builder.build();
  }
}
