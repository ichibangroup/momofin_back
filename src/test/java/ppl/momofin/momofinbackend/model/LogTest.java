package ppl.momofin.momofinbackend.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LogTest {

    @Test
    void testLogConstructor() {
        LocalDateTime timestamp = LocalDateTime.now();
        UUID userId = UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe");
        String level = "INFO";
        String message = "User Login Successful";
        String logName = "/auth/login";
        String sourceUrl = "http://localhost:8080/auth/login";

        Log log = new Log(userId, timestamp, level, message, logName, sourceUrl);

        assertEquals(timestamp, log.getTimestamp(), "Timestamp should Match");
        assertEquals(userId, log.getUserId(), "User ID should Match");
        assertEquals(level, log.getLevel(), "Level should Match");
        assertEquals(message, log.getMessage(), "Message should Match");
        assertEquals(logName, log.getLogName(), "Log Name should Match");
        assertEquals(sourceUrl, log.getSourceUrl(), "SourceURL should Match");
    }
}
