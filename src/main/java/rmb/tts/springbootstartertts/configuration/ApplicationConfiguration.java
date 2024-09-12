package rmb.tts.springbootstartertts.configuration;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;
import za.co.rmb.ocep.oceppep3client.configuration.Pep3ClientConfiguration;

@Slf4j
@Configuration
@AllArgsConstructor
@Import(Pep3ClientConfiguration.class)
public class ApplicationConfiguration {

  private final ApplicationConfigurationProperties properties;

  @Bean
  public RestTemplate defaultRestTemplate() {
    return new RestTemplate();
  }

  @Bean
  public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
    return registry -> registry.config().commonTags("application", "put-application-name-here");
  }
}
