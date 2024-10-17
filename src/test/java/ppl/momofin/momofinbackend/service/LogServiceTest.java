package ppl.momofin.momofinbackend.service;

import io.sentry.Sentry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ppl.momofin.momofinbackend.model.Log;
import ppl.momofin.momofinbackend.repository.LogRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class LogServiceTest {

    @Mock
    private LogRepository logRepository;

    @InjectMocks
    private LoggingServiceImpl loggingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLogMethod() {
        Long userID = 1L;
        String level = "ERROR";
        String message = "Failed Login Attempt";
        String logName = "/auth/login";
        String sourceUrl = "http://localhost/auth/login";

        loggingService.log(userID,level, message, logName, sourceUrl);

        ArgumentCaptor<Log> logCaptor = ArgumentCaptor.forClass(Log.class);
        verify(logRepository).save(logCaptor.capture());

        Log savedlog = logCaptor.getValue();
        assertEquals(userID, savedlog.getUserId(), "User ID Should Match");
        assertEquals(level, savedlog.getLevel(), "Log Level Should Match");
        assertEquals(message, savedlog.getMessage(), "Log Message Should Match");
        assertEquals(logName, savedlog.getLogName(), "Log Name Should Match");
        assertEquals(sourceUrl, savedlog.getSourceUrl(), "Source URL Should Match");
        assertNotNull(savedlog.getTimestamp(), "Timestamp Should Not Be Null");
        assertTrue(savedlog.getTimestamp().isBefore(LocalDateTime.now()) ||
                savedlog.getTimestamp().isEqual(LocalDateTime.now()), "Timestamp Should be Current");
    }

    @Test
    void testLogException() {
        Long userId = 1L;
        Exception exception = new RuntimeException("Test Exception");
        String logName = "/test/log";
        String sourceUrl = "http://localhost/test";

        mockStatic(Sentry.class);

        loggingService.logException(userId, exception, logName, sourceUrl);

        ArgumentCaptor<Log> logEntryCaptor = ArgumentCaptor.forClass(Log.class);

        verify(logRepository).save(logEntryCaptor.capture());

        Log capturedLog = logEntryCaptor.getValue();
        assertEquals(userId, capturedLog.getUserId());
        assertEquals(LocalDateTime.now().getHour(), capturedLog.getTimestamp().getHour());
        assertEquals("WARN", capturedLog.getLevel());
        assertEquals("Test Exception", capturedLog.getMessage());
        assertEquals(logName, capturedLog.getLogName());
        assertEquals(sourceUrl, capturedLog.getSourceUrl());

        verify(Sentry.class);
        Sentry.captureException(exception);
    }
}
