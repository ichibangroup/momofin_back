package ppl.momofin.momofinbackend.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ppl.momofin.momofinbackend.model.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private String companyname;
    private String password;
    private String email;
    private User user;


    @BeforeEach
    public void setup() {
        this.companyname = "test";
        this.email = "test@email.com";
        this.password = "password";
        this.user = new User();
    }

    @Test
    public void testFindWithEmail() {
        User user1  = new User(companyname,email,password);

        userRepository.save(user1);

        Optional<User> checkUser = userRepository.findByEmail("test@email.com");

        assertTrue(checkUser.isPresent());
        assertEquals("test@email.com", checkUser.get().getEmail());
    }

    @Test
    public void testFindWithCompanyName() {
        User user1  = new User(companyname,email,password);

        userRepository.save(user1);

        Optional<User> checkUser = userRepository.findByCompanyName("test");

        assertTrue(checkUser.isPresent());
        assertEquals("test", checkUser.get().getCompanyname());
    }

    @Test
    public void testUserSave() {
        User user1  = new User(companyname,email,password);

        User userSave = userRepository.save(user1);

        assertEquals("test", userSave.getCompanyname());
        assertEquals("test@example.com", userSave.getEmail());
    }
}
