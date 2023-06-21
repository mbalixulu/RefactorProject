package rmb.ocep.springbootstarterocep;



import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import rmb.ocep.springbootstarterocep.configuration.ApplicationConfigurationProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class TestClass {

    private ApplicationConfigurationProperties properties = new ApplicationConfigurationProperties();


    @Test
    void demoTestMethod() {
        assertTrue(true);
    }



    // Junit 5 test

    @Test
    public void bTestMethodAssertingTrue(){
        Assertions.assertTrue(true);
    }

    @Test
    public void checkConfigProperties(){

        Assertions.assertEquals(21600,properties.getLifespan());
      ;
    }

    CalculatorTest calculatorTest;

    @BeforeEach
    void setUp() {
        calculatorTest = new CalculatorTest();
    }

    @Test
    @DisplayName("Simple multiplication should work")
    void testMultiply() {
        assertEquals(20, calculatorTest.multiply(4, 5),
                "Regular multiplication should work");
    }

    @RepeatedTest(5)
    @DisplayName("Ensure correct handling of zero")
    void testMultiplyWithZero() {
        assertEquals(0, calculatorTest.multiply(0, 5), "Multiple with zero should be zero");
        assertEquals(0, calculatorTest.multiply(5, 0), "Multiple with zero should be zero");
    }
}

