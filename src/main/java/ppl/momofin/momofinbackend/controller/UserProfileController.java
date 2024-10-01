package ppl.momofin.momofinbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.service.UserService;
import ppl.momofin.momofinbackend.error.UserNotFoundException;

@RestController
@RequestMapping("/api/user")
public class UserProfileController {

    private final UserService userService;

    @Autowired
    public UserProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<User> getUserProfile(@PathVariable Long userId) {
        System.out.println("GET request received for user ID: " + userId);
        try {
            User user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            System.out.println("User not found: " + userId);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/profile/{userId}")
    public ResponseEntity<User> updateUserProfile(@PathVariable Long userId, @RequestBody User updatedUser) {
        System.out.println("POST request received for user ID: " + userId);
        System.out.println("Updated user data: " + updatedUser);
        try {
            User user = userService.updateUser(userId, updatedUser);
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            System.out.println("User not found: " + userId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.out.println("Error updating user: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}