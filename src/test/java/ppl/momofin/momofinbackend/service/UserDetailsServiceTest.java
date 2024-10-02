package ppl.momofin.momofinbackend.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
 class UserDetailsServiceTest {

    @Autowired
    private UserDetailsService userDetailsService;

    @Test
    void testUserDetailsServiceBean() {
        assertNotNull(userDetailsService);
    }
}