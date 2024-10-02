package ppl.momofin.momofinbackend.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LogTest {

    @Test
    void testLogConstructor() {
        LocalDateTime timestamp = LocalDateTime.now();
        String level = "INFO";
        String message = "User Login Successful";
        String log_name = "/auth/login";

        Log log = new Log(timestamp, level, message, log_name);

        assertEquals(timestamp, log.getTimestamp(), "Timestamp should Match");
        assertEquals(level, log.getLevel(), "Level should Match");
        assertEquals(message, log.getMessage(), "Message should Match");
        assertEquals(log_name, log.getLog_name(), "Log Name should Match");
    }
}
