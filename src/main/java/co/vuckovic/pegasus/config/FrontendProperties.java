package co.vuckovic.pegasus.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "frontend")
public class FrontendProperties {

  private String baseUrl;
}
