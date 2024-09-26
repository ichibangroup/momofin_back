package ppl.momofin.momofinbackend.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthResponseFailureTest {
    @Test
    void testAuthResponseFailureConstructorEmpty() {
        AuthResponseFailure responseFailure = new AuthResponseFailure();

        assertNotNull(responseFailure);
        assertNull(responseFailure.getErrorMessage());
    }

    @Test
    void testAuthResponseFailureConstructor() {
        String errorMessage = "Invalid credentials";
        AuthResponseFailure responseFailure = new AuthResponseFailure(errorMessage);

        assertEquals(errorMessage, responseFailure.getErrorMessage());
    }

    @Test
    void testGetSetJwt() {
        String errorMessage = "Invalid credentials";
        AuthResponseFailure responseFailure = new AuthResponseFailure();

        responseFailure.setErrorMessage(errorMessage);
        assertEquals(errorMessage, responseFailure.getErrorMessage());
    }
}
