package ppl.momofin.momofinbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    public void setUp() {
        user = new User("TestCompany", "test@example.com", "password123");
    }

    @Test
    public void testFindWithEmail() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        Optional<User> foundUser = userService.findByEmailOrCompanyName("test@example.com");

        assertTrue(foundUser.isPresent());
        assertEquals("TestCompany", foundUser.get().getCompanyName());
    }

    @Test
    public void testFindWithCompanyName() {
        when(userRepository.findByCompanyName("TestCompany")).thenReturn(Optional.of(user));

        Optional<User> foundUser = userService.findByEmailOrCompanyName("TestCompany");

        assertTrue(foundUser.isPresent());
        assertEquals("test@example.com", foundUser.get().getEmail());
    }

    @Test
    public void testValidPassword() {
        user.setPassword("password123");

        boolean isValid = userService.validatePassword(user, "password123");

        assertTrue(isValid);
    }

    @Test
    public void testCheckPassword_InvalidPassword() {
        boolean isValid = userService.validatePassword(user, "wrongPassword");

        assertFalse(isValid);
    }
}
