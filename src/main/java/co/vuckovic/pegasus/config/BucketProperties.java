package co.vuckovic.pegasus.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "bucket")
public class BucketProperties {

  @Value("${bucket.folder-path}")
  private String folderPath;
  @Value("${bucket.initial-size}")
  private Double initialSize;
  @Value("${bucket.capacity}")
  private Double capacity;

}