package rmb.ocep.springbootstarterocep;



import lombok.Getter;
import org.junit.jupiter.api.*;
import rmb.ocep.springbootstarterocep.configuration.ApplicationConfigurationProperties;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Getter
public class TestClass {

     ApplicationConfigurationProperties properties = new ApplicationConfigurationProperties();

//Junit 4
    @Test
    void demoTestMethod() {
        assertTrue(true);
    }

    @Test
    public void bTestMethodAssertingTrue(){
        Assertions.assertTrue(true);
    }

    @Test
    public void checkConfigProperties(){

        //Junit 5
        Assertions.assertEquals(0,properties.getLifespan());
        // Junit 4
        assertEquals(0, properties.getLifespan());

    }

}

