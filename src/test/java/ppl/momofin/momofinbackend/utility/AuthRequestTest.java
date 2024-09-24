package ppl.momofin.momofinbackend.utility;

import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuthRequestTest {
    @Test
    void testEmptyConstructor() {
        AuthRequest authRequest = new AuthRequest();
        assertNotNull(authRequest);
        assertNull(authRequest.getOrganizationName());
        assertNull(authRequest.getEmail());
        assertNull(authRequest.getPassword);
    }

    @Test
    void testGetSetOrganizationName() {
        String organizationName = "Momofin";
        AuthRequest authRequest = new AuthRequest();
        authRequest.setOrganizationName(organizationName);
        assertEquals(organizationName, authRequest.getOrganizationName());
    }

    @Test
    void testGetSetEmail() {
        String email = "test@email.com";
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail(email);
        assertEquals(email, authRequest.getEmail());
    }

    @Test
    void testGetSetPassword() {
        String password = "test password";
        AuthRequest authRequest = new AuthRequest();
        authRequest.setPassword(password);
        assertEquals(password, authRequest.getPassword());
    }
}
