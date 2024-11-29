package ppl.momofin.momofinbackend.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ppl.momofin.momofinbackend.response.ErrorResponse;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    @Mock
    private Logger mockLogger; // Mock the logger

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        // Initialize the GlobalExceptionHandler
        globalExceptionHandler = new GlobalExceptionHandler();

        // Use reflection to replace the static logger field with the mocked one
        java.lang.reflect.Field field = GlobalExceptionHandler.class.getDeclaredField("logger");
        field.setAccessible(true); // Make the private field accessible
        field.set(null, mockLogger); // Set the static field to the mock logger
    }

    @Test
    void handleIllegalArgumentException_shouldLogAndReturnErrorResponse() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid input");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid request: Invalid input", Objects.requireNonNull(response.getBody()).getErrorMessage());

        // Capturing log arguments
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
        // Given
        IllegalArgumentException exception = new IllegalArgumentException();

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid request: No details available", response.getBody().getErrorMessage());

        // Capturing log arguments
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
        // Given
        Exception exception = new Exception("System failure");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred. Please try again later.", response.getBody().getErrorMessage());

        // Capturing log arguments
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
        // Given
        Exception exception = new Exception();

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred. Please try again later.", response.getBody().getErrorMessage());

        // Capturing log arguments
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
        // Given
        String largeMessage = "a".repeat(1000); // Creating a large message
        IllegalArgumentException exception = new IllegalArgumentException(largeMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid request: " + largeMessage, response.getBody().getErrorMessage());

        // Capturing log arguments
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> argCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Throwable> exceptionCaptor = ArgumentCaptor.forClass(Throwable.class);

        verify(mockLogger).error(messageCaptor.capture(), argCaptor.capture(), exceptionCaptor.capture());
        assertEquals("IllegalArgumentException occurred: {}", messageCaptor.getValue());
        assertEquals(largeMessage, argCaptor.getValue());
        assertEquals(exception, exceptionCaptor.getValue());
    }
}
