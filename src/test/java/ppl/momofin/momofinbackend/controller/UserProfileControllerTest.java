package ppl.momofin.momofinbackend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.service.UserService;
import ppl.momofin.momofinbackend.error.UserNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UserProfileControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserProfileController userProfileController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUserProfile_ExistingUser_ReturnsUser() {
        Long userId = 1L;
        User mockUser = new User(userId, "testuser", "test@example.com");
        when(userService.getUserById(userId)).thenReturn(mockUser);

        ResponseEntity<User> response = userProfileController.getUserProfile(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockUser, response.getBody());
        verify(userService).getUserById(userId);
    }

    @Test
    void getUserProfile_NonExistingUser_ReturnsNotFound() {
        Long userId = 1L;
        when(userService.getUserById(userId)).thenThrow(new UserNotFoundException("User not found"));

        ResponseEntity<User> response = userProfileController.getUserProfile(userId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService).getUserById(userId);
    }

    @Test
    void updateUserProfile_ExistingUser_ReturnsUpdatedUser() {
        Long userId = 1L;
        User updatedUser = new User(userId, "updateduser", "updated@example.com");
        when(userService.updateUser(eq(userId), any(User.class))).thenReturn(updatedUser);

        ResponseEntity<User> response = userProfileController.updateUserProfile(userId, updatedUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedUser, response.getBody());
        verify(userService).updateUser(eq(userId), any(User.class));
    }

    @Test
    void updateUserProfile_NonExistingUser_ReturnsNotFound() {
        Long userId = 1L;
        User updatedUser = new User(userId, "updateduser", "updated@example.com");
        when(userService.updateUser(eq(userId), any(User.class))).thenThrow(new UserNotFoundException("User not found"));

        ResponseEntity<User> response = userProfileController.updateUserProfile(userId, updatedUser);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService).updateUser(eq(userId), any(User.class));
    }
}