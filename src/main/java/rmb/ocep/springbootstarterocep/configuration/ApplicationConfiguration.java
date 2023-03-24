package rmb.ocep.springbootstarterocep.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;
import za.co.rmb.ocep.oceppep3client.configuration.Pep3ClientConfiguration;

@Slf4j
@Configuration
@Import(Pep3ClientConfiguration.class)
public class ApplicationConfiguration {
  private ApplicationConfigurationProperties properties;

  @Autowired
  public ApplicationConfiguration(final ApplicationConfigurationProperties properties) {
    this.properties = properties;
  }

  @Bean
  public RestTemplate defaultRestTemplate() {
    return new RestTemplate();
  }
}
