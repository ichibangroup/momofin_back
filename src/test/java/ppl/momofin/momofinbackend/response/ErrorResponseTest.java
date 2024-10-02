package ppl.momofin.momofinbackend.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {
    @Test
    void testErrorResponseFailureConstructorEmpty() {
        ErrorResponse responseFailure = new ErrorResponse();

        assertNotNull(responseFailure);
        assertNull(responseFailure.getErrorMessage());
    }

    @Test
    void testErrorResponseFailureConstructor() {
        String errorMessage = "Invalid credentials";
        ErrorResponse responseFailure = new ErrorResponse(errorMessage);

        assertEquals(errorMessage, responseFailure.getErrorMessage());
    }

    @Test
    void testGetSetErrorMessage() {
        String errorMessage = "Invalid credentials";
        ErrorResponse responseFailure = new ErrorResponse();

        responseFailure.setErrorMessage(errorMessage);
        assertEquals(errorMessage, responseFailure.getErrorMessage());
    }
}
