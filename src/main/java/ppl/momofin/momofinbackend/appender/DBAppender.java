package ppl.momofin.momofinbackend.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import lombok.Setter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Setter
public class DBAppender extends AppenderBase<ILoggingEvent> {

    private String url;
    private String username;
    private String password;

    private Connection connection;

    @Override
    public void start() {
        try {
            connection = DriverManager.getConnection(url, username, password);
            super.start();
        } catch (SQLException e) {
            addError("Failed to connect to the database", e);
        }
    }

    @Override
    protected void append (ILoggingEvent event) {
        if (connection != null) {
            try {
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO log_table (timestamp, level, logger, message) VALUES (CURRENT_TIMESTAMP, ?, ?, ?)"
                );
                statement.setString(1, event.getLevel().toString());
                statement.setString(2, event.getLoggerName());
                statement.setString(3, event.getFormattedMessage());
                statement.executeUpdate();
            } catch (SQLException e) {
                addError("Failed to insert log into database", e);
            }
        }
    }

    @Override
    public void stop() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                addError("Failed to close database connection", e);
            }
        }
        super.stop();
    }
}
