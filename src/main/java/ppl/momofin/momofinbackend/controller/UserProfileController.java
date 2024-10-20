package ppl.momofin.momofinbackend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ppl.momofin.momofinbackend.error.InvalidPasswordException;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.service.UserService;
import ppl.momofin.momofinbackend.error.UserNotFoundException;
import org.springframework.web.bind.annotation.RestController;


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

    @PutMapping("/profile/{userId}")
    public ResponseEntity<Object> updateUserProfile(
            @PathVariable Long userId,
            @RequestBody User updatedUser,
            @RequestParam(required = false) String oldPassword,
            @RequestParam(required = false) String newPassword) {

        logger.info("Received update request for user ID: {}", userId);

        try {
            User user = userService.updateUser(userId, updatedUser, oldPassword, newPassword);
            logger.info("User profile updated successfully for user ID: {}", userId);
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            logger.warn("User not found: {}", userId);
            return ResponseEntity.notFound().build();
        } catch (InvalidPasswordException e) {
            logger.warn("Invalid password for user ID: {}", userId);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating user: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("An unexpected error occurred");
        }
    }
}