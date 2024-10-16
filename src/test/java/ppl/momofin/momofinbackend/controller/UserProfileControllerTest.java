package ppl.momofin.momofinbackend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
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

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockUser, response.getBody());
        verify(userService).getUserById(userId);
    }

    @Test
    void getUserProfile_ReturnsNotFound_WhenUserDoesNotExist() {
        Long userId = 1L;
        when(userService.getUserById(userId)).thenThrow(new UserNotFoundException("User not found"));

        ResponseEntity<User> response = userProfileController.getUserProfile(userId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService).getUserById(userId);
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

        assertEquals(HttpStatus.OK, response.getStatusCode());
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

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService).updateUser(eq(userId), any(User.class));
    }

    @Test
    void updateUserProfile_ReturnsInternalServerError_WhenUnexpectedErrorOccurs() {
        Long userId = 1L;
        User updatedUser = new User();
        updatedUser.setUserId(userId);

        when(userService.updateUser(eq(userId), any(User.class))).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<User> response = userProfileController.updateUserProfile(userId, updatedUser);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userService).updateUser(eq(userId), any(User.class));
    }
    @Test
    void updateUserProfile_ReturnsBadRequest_WhenOldPasswordIsIncorrect() {
        Long userId = 1L;
        User updatedUser = new User();
        updatedUser.setUserId(userId);
        updatedUser.setUsername("updatedUsername");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setOldPassword("incorrectPassword");
        updatedUser.setNewPassword("newPassword");

        when(userService.updateUser(eq(userId), any(User.class)))
                .thenThrow(new InvalidPasswordException("Invalid old password"));

        ResponseEntity<?> response = userProfileController.updateUserProfile(userId, updatedUser);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid old password", response.getBody());
        verify(userService).updateUser(eq(userId), any(User.class));
    }

    @Test
    void updateUserProfile_ReturnsOk_WhenOldPasswordIsCorrect() {
        Long userId = 1L;
        User updatedUser = new User();
        updatedUser.setUserId(userId);
        updatedUser.setUsername("updatedUsername");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setOldPassword("correctPassword");
        updatedUser.setNewPassword("newPassword");

        User returnedUser = new User();
        returnedUser.setUserId(userId);
        returnedUser.setUsername("updatedUsername");
        returnedUser.setEmail("updated@example.com");

        when(userService.updateUser(eq(userId), any(User.class))).thenReturn(returnedUser);

        ResponseEntity<?> response = userProfileController.updateUserProfile(userId, updatedUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(returnedUser, response.getBody());
        verify(userService).updateUser(eq(userId), any(User.class));
    }
}