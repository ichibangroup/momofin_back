package ppl.momofin.momofinbackend;

import io.sentry.spring.jakarta.EnableSentry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@EnableSentry(dsn = "https://add63d98e02c0a719872a1832a6df407@o4508102015582208.ingest.de.sentry.io/4508102038716496")
@Configuration
public class MomofinbackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MomofinbackendApplication.class, args);
    }

}
