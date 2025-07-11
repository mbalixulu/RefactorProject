package za.co.rmb.tts.mandates.resolutions.ui;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import za.co.rmb.tts.mandates.resolutions.ui.configuration.ApplicationConfigurationProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("local")
class MandatesResolutionsUITTSApplicationTests {

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
