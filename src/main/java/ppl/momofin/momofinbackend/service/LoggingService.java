package ppl.momofin.momofinbackend.service;

import java.util.UUID;

public interface LoggingService {
    void log(UUID userId, String level, String message, String logName, String sourceUrl);
    void logException(UUID userId, Exception e, String logName, String sourceUrl);
}

