package ppl.momofin.momofinbackend.dto;

import org.junit.jupiter.api.Test;
import ppl.momofin.momofinbackend.model.User;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserDTOTest {

    @Test
    void fromUser_ShouldCreateUserDTOCorrectly() {
        User user = new User(null, "testuser", "Test User", "test@example.com", "password", "Developer", false);
        user.setUserId(UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe"));

        UserDTO userDTO = UserDTO.fromUser(user);

        assertEquals(UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe"), userDTO.getUserId());
        assertEquals("testuser", userDTO.getUsername());
        assertEquals("Test User", userDTO.getName());
        assertEquals("test@example.com", userDTO.getEmail());
        assertEquals("Developer", userDTO.getPosition());
        assertFalse(userDTO.isOrganizationAdmin());
    }

    @Test
    void toUser_ShouldCreateUserCorrectly() {
        UserDTO userDTO = new UserDTO(UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe"), "testuser", "Test User", "test@example.com", "Developer", false, false);

        User user = userDTO.toUser();

        assertEquals(UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe"), user.getUserId());
        assertEquals("testuser", user.getUsername());
        assertEquals("Test User", user.getName());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Developer", user.getPosition());
        assertFalse(user.isOrganizationAdmin());
        assertNull(user.getPassword());
    }
}