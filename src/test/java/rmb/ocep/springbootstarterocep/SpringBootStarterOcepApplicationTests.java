package rmb.ocep.springbootstarterocep;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import rmb.ocep.springbootstarterocep.configuration.ApplicationConfigurationProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("local")
public class SpringBootStarterOcepApplicationTests {

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
  public void checkConfigProperties() {
    assertEquals(21600, properties.getLifespan());
  }

}
