package ppl.momofin.momofinbackend.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ppl.momofin.momofinbackend.response.ErrorResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;
    private Logger mockLogger;

    @BeforeEach
    void setUp() {
        mockLogger = mock(Logger.class);
        globalExceptionHandler = new GlobalExceptionHandler(mockLogger);
    }

    @Test
    void handleIllegalArgumentException_shouldLogAndReturnErrorResponse() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid input");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid request: Invalid input", response.getBody().getErrorMessage());

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> argCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Throwable> exceptionCaptor = ArgumentCaptor.forClass(Throwable.class);

        verify(mockLogger).error(messageCaptor.capture(), argCaptor.capture(), exceptionCaptor.capture());
        assertEquals("IllegalArgumentException occurred: {}", messageCaptor.getValue());
        assertEquals("Invalid input", argCaptor.getValue());
        assertEquals(exception, exceptionCaptor.getValue());
    }

    @Test
    void handleIllegalArgumentException_shouldHandleNullMessageGracefully() {
        IllegalArgumentException exception = new IllegalArgumentException();

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid request: No details available", response.getBody().getErrorMessage());

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> argCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Throwable> exceptionCaptor = ArgumentCaptor.forClass(Throwable.class);

        verify(mockLogger).error(messageCaptor.capture(), argCaptor.capture(), exceptionCaptor.capture());
        assertEquals("IllegalArgumentException occurred: {}", messageCaptor.getValue());
        assertEquals("No details available", argCaptor.getValue());
        assertEquals(exception, exceptionCaptor.getValue());
    }

    @Test
    void handleGenericException_shouldLogAndReturnErrorResponse() {
        Exception exception = new Exception("System failure");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred. Please try again later.", response.getBody().getErrorMessage());

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> argCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Throwable> exceptionCaptor = ArgumentCaptor.forClass(Throwable.class);

        verify(mockLogger).error(messageCaptor.capture(), argCaptor.capture(), exceptionCaptor.capture());
        assertEquals("Unexpected error occurred: {}", messageCaptor.getValue());
        assertEquals("System failure", argCaptor.getValue());
        assertEquals(exception, exceptionCaptor.getValue());
    }

    @Test
    void handleGenericException_shouldHandleNullMessageGracefully() {
        Exception exception = new Exception();

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred. Please try again later.", response.getBody().getErrorMessage());

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> argCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Throwable> exceptionCaptor = ArgumentCaptor.forClass(Throwable.class);

        verify(mockLogger).error(messageCaptor.capture(), argCaptor.capture(), exceptionCaptor.capture());
        assertEquals("Unexpected error occurred: {}", messageCaptor.getValue());
        assertEquals("No details available", argCaptor.getValue()); // Matches the default message for null
        assertEquals(exception, exceptionCaptor.getValue());
    }

    @Test
    void handleIllegalArgumentException_shouldHandleLargeErrorMessages() {
        String largeMessage = "a".repeat(1000);
        IllegalArgumentException exception = new IllegalArgumentException(largeMessage);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid request: " + largeMessage, response.getBody().getErrorMessage());

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> argCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Throwable> exceptionCaptor = ArgumentCaptor.forClass(Throwable.class);

        verify(mockLogger).error(messageCaptor.capture(), argCaptor.capture(), exceptionCaptor.capture());
        assertEquals("IllegalArgumentException occurred: {}", messageCaptor.getValue());
        assertEquals(largeMessage, argCaptor.getValue());
        assertEquals(exception, exceptionCaptor.getValue());
    }
}
