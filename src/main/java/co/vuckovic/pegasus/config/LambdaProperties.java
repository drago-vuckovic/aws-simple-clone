package co.vuckovic.pegasus.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "lambda-service")
public class LambdaProperties {

  private String baseUrl;

  private String dummyUrl;
}
