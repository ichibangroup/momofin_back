package ppl.momofin.momofinbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ppl.momofin.momofinbackend.model.Log;
import ppl.momofin.momofinbackend.repository.LogRepository;

import java.time.LocalDateTime;

@Service
public class LoggingServiceImpl implements LoggingService{

    @Autowired
    private LogRepository logRepository;

    @Override
    public void log(String level, String message) {
        Log logEntry = new Log(LocalDateTime.now(), level, message);
        logRepository.save(logEntry);
    }
}
