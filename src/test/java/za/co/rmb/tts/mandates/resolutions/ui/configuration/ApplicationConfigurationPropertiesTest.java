package za.co.rmb.tts.mandates.resolutions.ui.configuration;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("local")
class ApplicationConfigurationPropertiesTest {

  @Autowired
  private ApplicationConfigurationProperties applicationProperties;

  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;

  @BeforeEach
  void setUp() {
    System.setOut(new PrintStream(outContent));
  }

  @Test
  void printSystemInfo() {
    applicationProperties.printSystemInfo();

    String string = outContent.toString();
    boolean contains = string.contains("""
        ===========CONFIG=========
        mandates-resolutions-ui: [21600]
        """);
    assertTrue(contains);
  }

  @AfterEach
  void restoreStreams() {
    System.setOut(originalOut);
  }
}
