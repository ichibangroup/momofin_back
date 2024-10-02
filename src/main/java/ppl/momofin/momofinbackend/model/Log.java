package ppl.momofin.momofinbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "logs")
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;
    private String level;
    private String message;
    private String log_name;

    public Log(LocalDateTime timestamp, String level, String message, String log_name) {
        this.timestamp = timestamp;
        this.level = level;
        this.message = message;
        this.log_name = log_name;
    }

    public Log() {

    }
}
