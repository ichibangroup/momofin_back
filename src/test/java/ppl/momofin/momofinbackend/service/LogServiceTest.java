package ppl.momofin.momofinbackend.service;

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
public class LogServiceTest {

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
        String level = "ERROR";
        String message = "Failed Login Attempt";

        loggingService.log(level, message);

        ArgumentCaptor<Log> logCaptor = ArgumentCaptor.forClass(Log.class);
        verify(logRepository).save(logCaptor.capture());

        Log savedlog = logCaptor.getValue();
        assertEquals(level, savedlog.getLevel(), "Log Level Should Match");
        assertEquals(message, savedlog.getMessage(), "Log Message Should Match");
        assertNotNull(savedlog.getTimestamp(), "Timestamp Should Not Be Null");
        assertTrue(savedlog.getTimestamp().isBefore(LocalDateTime.now()) ||
                savedlog.getTimestamp().isEqual(LocalDateTime.now()), "Timestamp Should be Current");
    }
}
