package za.co.rmb.tts.springbootstartertts.configuration;

import jakarta.annotation.PostConstruct;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import za.co.rmb.tts.javasharedutilities.util.UrlUtil;

@Configuration
@Slf4j
@Getter
public class ApplicationConfigurationProperties {

  @Value("${cache.lifespan}")
  private long lifespan;

  @Value("${spring-boot-starter-tts.host}")
  private String springBootStarterTTSHost;

  @PostConstruct
  public void printSystemInfo() {
    log.info("\n===========CONFIG=========\n" + "spring-boot-starter-tts: [{}]\n", this.lifespan);
  }

  @Bean
  public UrlUtil getUrlUtil() {
    return new UrlUtil();
  }
}
