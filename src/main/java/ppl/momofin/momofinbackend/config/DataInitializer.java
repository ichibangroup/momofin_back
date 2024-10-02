package ppl.momofin.momofinbackend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ppl.momofin.momofinbackend.service.OrganizationService;

@Configuration
public class DataInitializer {

    @Autowired
    private OrganizationService organizationService;

    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            organizationService.populateNullDescriptions();
        };
    }
}
