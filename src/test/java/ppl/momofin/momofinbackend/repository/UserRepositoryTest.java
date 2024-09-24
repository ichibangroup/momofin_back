package ppl.momofin.momofinbackend.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ppl.momofin.momofinbackend.model.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindWithEmail() {
        String companyName = "test";
        String email = "test@email.com";
        String password = "password";
        User user = new User(companyName, email, password);

        userRepository.save(user);

        Optional<User> checkUser = userRepository.findByEmail(email);

        assertTrue(checkUser.isPresent());
        assertEquals(email, checkUser.get().getEmail());
    }

    @Test
    public void testFindWithCompanyName() {
        String companyName = "test";
        String email = "test@email.com";
        String password = "password";
        User user = new User(companyName, email, password);

        userRepository.save(user);

        Optional<User> checkUser = userRepository.findByCompanyName(companyName);

        assertTrue(checkUser.isPresent());
        assertEquals(companyName, checkUser.get().getCompanyName());
    }

    @Test
    public void testUserSave() {
        String companyName = "test";
        String email = "test@email.com";
        String password = "password";
        User user = new User(companyName, email, password);

        User userSave = userRepository.save(user);

        assertEquals(companyName, userSave.getCompanyName());
        assertEquals(email, userSave.getEmail());
    }
}
