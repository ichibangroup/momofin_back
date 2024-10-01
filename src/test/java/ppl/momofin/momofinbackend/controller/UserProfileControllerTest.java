package ppl.momofin.momofinbackend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.service.UserService;

import java.util.Optional;

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
        when(userService.getUserById(userId)).thenReturn(Optional.of(mockUser));

        ResponseEntity<User> response = userProfileController.getUserProfile(userId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockUser, response.getBody());
    }
}