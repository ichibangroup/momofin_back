package ppl.momofin.momofinbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.repository.UserRepository;
import ppl.momofin.momofinbackend.error.UserNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUserById_ExistingUser_ReturnsUser() {
        Long userId = 1L;
        User mockUser = new User(userId, "testuser", "test@example.com");
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        User result = userService.getUserById(userId);

        assertEquals(mockUser, result);
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_NonExistingUser_ThrowsUserNotFoundException() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(userId));
        verify(userRepository).findById(userId);
    }

    @Test
    void updateUser_ExistingUser_ReturnsUpdatedUser() {
        Long userId = 1L;
        User existingUser = new User(userId, "olduser", "old@example.com");
        User updatedUser = new User(userId, "newuser", "new@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        User result = userService.updateUser(userId, updatedUser);

        assertEquals(updatedUser, result);
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_NonExistingUser_ThrowsUserNotFoundException() {
        Long userId = 1L;
        User updatedUser = new User(userId, "newuser", "new@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(userId, updatedUser));
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }
}