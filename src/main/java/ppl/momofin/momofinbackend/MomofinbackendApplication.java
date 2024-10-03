package ppl.momofin.momofinbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class MomofinbackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MomofinbackendApplication.class, args);
    }

}
