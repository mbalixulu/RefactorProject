package rmb.tts.springbootstartertts.configuration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApplicationConfigurationPropertiesTest {

  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private ApplicationConfigurationProperties applicationProperties;

  @BeforeEach
  void setUp() {
    applicationProperties = new ApplicationConfigurationProperties();
    System.setOut(new PrintStream(outContent));
  }

  @Test
  void testLoggedOutCode() {
    applicationProperties.printSystemInfo();

    String string = outContent.toString();
    boolean contains = string.contains("===========CONFIG=========\n"
        + "spring-boot-starter-tts:");
    assertTrue(contains);
  }

  @AfterEach
  public void restoreStreams() {
    System.setOut(originalOut);
  }
}
