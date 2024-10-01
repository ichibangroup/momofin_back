package ppl.momofin.momofinbackend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import ppl.momofin.momofinbackend.error.UserNotFoundException;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.service.UserService;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

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
    void getUserProfile_ReturnsUser_WhenUserExists() {
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setUserId(userId);
        when(userService.getUserById(userId)).thenReturn(mockUser);

        ResponseEntity<User> response = userProfileController.getUserProfile(userId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockUser, response.getBody());
    }

    @Test
    void updateUserProfile_ReturnsUpdatedUser_WhenUserExists() {
        Long userId = 1L;
        User updatedUser = new User();
        updatedUser.setUserId(userId);
        updatedUser.setName("Updated Name");
        updatedUser.setEmail("updated@example.com");

        when(userService.updateUser(eq(userId), any(User.class))).thenReturn(updatedUser);

        ResponseEntity<User> response = userProfileController.updateUserProfile(userId, updatedUser);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(updatedUser, response.getBody());
        verify(userService).updateUser(eq(userId), any(User.class));
    }

    @Test
    void updateUserProfile_ReturnsNotFound_WhenUserDoesNotExist() {
        Long userId = 1L;
        User updatedUser = new User();
        updatedUser.setUserId(userId);

        when(userService.updateUser(eq(userId), any(User.class))).thenThrow(new UserNotFoundException("User not found"));

        ResponseEntity<User> response = userProfileController.updateUserProfile(userId, updatedUser);

        assertEquals(404, response.getStatusCodeValue());
        verify(userService).updateUser(eq(userId), any(User.class));
    }

}