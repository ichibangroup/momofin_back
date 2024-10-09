package ppl.momofin.momofinbackend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.service.UserService;
import ppl.momofin.momofinbackend.error.UserNotFoundException;

@RestController
@RequestMapping("/api/user")
public class UserProfileController {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);

    private final UserService userService;

    @Autowired
    public UserProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<User> getUserProfile(@PathVariable Long userId) {
        logger.info("GET request received for user ID: {}", userId);
        try {
            User user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            logger.warn("User not found: {}", userId);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/profile/{userId}")
    public ResponseEntity<User> updateUserProfile(@PathVariable Long userId, @RequestBody User updatedUser) {
        logger.info("POST request received for user ID: {}", userId);
        logger.debug("Updated user data: {}", updatedUser);
        try {
            User user = userService.updateUser(userId, updatedUser);
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            logger.warn("User not found: {}", userId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating user: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}