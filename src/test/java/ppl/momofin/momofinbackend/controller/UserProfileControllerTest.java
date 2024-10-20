package ppl.momofin.momofinbackend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ppl.momofin.momofinbackend.error.UserNotFoundException;
import ppl.momofin.momofinbackend.error.InvalidPasswordException;
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
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";

        when(userService.updateUser(eq(userId), any(User.class), eq(oldPassword), eq(newPassword))).thenReturn(updatedUser);

        ResponseEntity<?> response = userProfileController.updateUserProfile(userId, updatedUser, oldPassword, newPassword);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedUser, response.getBody());
        verify(userService).updateUser(eq(userId), any(User.class), eq(oldPassword), eq(newPassword));
    }

    @Test
    void updateUserProfile_ReturnsNotFound_WhenUserDoesNotExist() {
        Long userId = 1L;
        User updatedUser = new User();
        updatedUser.setUserId(userId);
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";

        when(userService.updateUser(eq(userId), any(User.class), eq(oldPassword), eq(newPassword)))
                .thenThrow(new UserNotFoundException("User not found"));

        ResponseEntity<?> response = userProfileController.updateUserProfile(userId, updatedUser, oldPassword, newPassword);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService).updateUser(eq(userId), any(User.class), eq(oldPassword), eq(newPassword));
    }

    @Test
    void updateUserProfile_ReturnsInternalServerError_WhenUnexpectedErrorOccurs() {
        Long userId = 1L;
        User updatedUser = new User();
        updatedUser.setUserId(userId);
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";

        when(userService.updateUser(eq(userId), any(User.class), eq(oldPassword), eq(newPassword)))
                .thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<?> response = userProfileController.updateUserProfile(userId, updatedUser, oldPassword, newPassword);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userService).updateUser(eq(userId), any(User.class), eq(oldPassword), eq(newPassword));
    }

    @Test
    void updateUserProfile_ReturnsBadRequest_WhenInvalidPasswordExceptionOccurs() {
        Long userId = 1L;
        User updatedUser = new User();
        updatedUser.setUserId(userId);
        String oldPassword = "wrongOldPassword";
        String newPassword = "newPassword";

        when(userService.updateUser(eq(userId), any(User.class), eq(oldPassword), eq(newPassword)))
                .thenThrow(new InvalidPasswordException("Invalid old password"));

        ResponseEntity<?> response = userProfileController.updateUserProfile(userId, updatedUser, oldPassword, newPassword);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid old password", response.getBody());
        verify(userService).updateUser(eq(userId), any(User.class), eq(oldPassword), eq(newPassword));
    }


}