package rmb.tts.springbootstartertts;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import rmb.tts.springbootstartertts.configuration.ApplicationConfigurationProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("local")
class SpringBootStarterTTSApplicationTests {

  @Autowired
  private ApplicationConfigurationProperties properties;

  @Test
  void contextLoads() {
  }

  @Test
  void demoTestMethod() {
    assertTrue(true);
  }

  @Test
  void checkConfigProperties() {
    assertEquals(21600, properties.getLifespan());
  }

}
