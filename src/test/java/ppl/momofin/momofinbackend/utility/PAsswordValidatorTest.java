package ppl.momofin.momofinbackend.utility;

import org.junit.jupiter.api.Test;
import ppl.momofin.momofinbackend.error.InvalidPasswordException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

public class PAsswordValidatorTest {
    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<PasswordValidator> constructor = PasswordValidator.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        Exception exception = assertThrows(InvocationTargetException.class, constructor::newInstance);

        Throwable cause = exception.getCause();
        assertInstanceOf(IllegalStateException.class, cause);
        assertEquals("Utility class", cause.getMessage());
    }

    @Test
    void testNullPassword() {
        assertThrows(InvalidPasswordException.class, () -> {
            PasswordValidator.validatePassword(null);
        });
    }

    @Test
    void testShortPassword() {
        String shortPassword = "123456789";
        assertThrows(InvalidPasswordException.class, () -> {
            PasswordValidator.validatePassword(shortPassword);
        });
    }

    @Test
    void testMinLengthPassword() {
        String minLengthPassword = "1234567890";
        assertDoesNotThrow(() -> {
            PasswordValidator.validatePassword(minLengthPassword);
        });
    }

    @Test
    void testLongPassword() {
        String longPassword = "12345678901";
        assertDoesNotThrow(() -> {
            PasswordValidator.validatePassword(longPassword);
        });
    }
}
