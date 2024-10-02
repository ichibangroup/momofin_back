package ppl.momofin.momofinbackend.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LogTest {

    @Test
    void testLogConstructor() {
        LocalDateTime timestamp = LocalDateTime.now();
        String level = "INFO";
        String message = "User Login Successful";
        String logName = "/auth/login";

        Log log = new Log(timestamp, level, message, logName);

        assertEquals(timestamp, log.getTimestamp(), "Timestamp should Match");
        assertEquals(level, log.getLevel(), "Level should Match");
        assertEquals(message, log.getMessage(), "Message should Match");
        assertEquals(logName, log.getLogName(), "Log Name should Match");
    }
}
