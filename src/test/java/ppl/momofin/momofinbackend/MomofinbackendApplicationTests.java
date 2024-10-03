package ppl.momofin.momofinbackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MomofinbackendApplicationTests {

    @Test
    void contextLoads() {
        // This test is to see if the application itself can load properly, by using the @SpringBootTest decorator
    }

    @Test
    void testMain() {
        // Test the main method to ensure it doesn't throw any exceptions
        MomofinbackendApplication.main(new String[]{});
    }

}
