package ppl.momofin.momofinbackend.dto;

import org.junit.jupiter.api.Test;
import ppl.momofin.momofinbackend.model.User;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserDTOTest {

    @Test
    void fromUser_ShouldCreateUserDTOCorrectly() {
        User user = new User(null, "testuser", "Test User", "test@example.com", "password", "Developer", false);
        UUID userId = UUID.fromString("ff354956-c4c4-4697-9814-e34cd5ef5d4b");
        user.setUserId(userId);

        UserDTO userDTO = UserDTO.fromUser(user);

        assertEquals(userId, userDTO.getUserId());
        assertEquals("testuser", userDTO.getUsername());
        assertEquals("Test User", userDTO.getName());
        assertEquals("test@example.com", userDTO.getEmail());
        assertEquals("Developer", userDTO.getPosition());
        assertFalse(userDTO.isOrganizationAdmin());
    }

    @Test
    void toUser_ShouldCreateUserCorrectly() {
        UUID userId = UUID.fromString("ff354956-c4c4-4697-9814-e34cd5ef5d4b");
        UserDTO userDTO = new UserDTO(userId, "testuser", "Test User", "test@example.com", "Developer", false);

        User user = userDTO.toUser();

        assertEquals(userId, user.getUserId());
        assertEquals("testuser", user.getUsername());
        assertEquals("Test User", user.getName());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Developer", user.getPosition());
        assertFalse(user.isOrganizationAdmin());
        assertNull(user.getPassword());
    }
}