package ppl.momofin.momofinbackend.utility;

import org.junit.jupiter.api.Test;
import ppl.momofin.momofinbackend.request.AuthRequest;


import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuthRequestTest {
    @Test
    void testEmptyConstructor() {
        AuthRequest authRequest = new AuthRequest();
        assertNotNull(authRequest);
        assertNull(authRequest.getOrganizationName());
        assertNull(authRequest.getUsername());
        assertNull(authRequest.getPassword());
    }

    @Test
    void testGetSetOrganizationName() {
        String organizationName = "Momofin";
        AuthRequest authRequest = new AuthRequest();
        authRequest.setOrganizationName(organizationName);
        assertEquals(organizationName, authRequest.getOrganizationName());
    }

    @Test
    void testGetSetUsername() {
        String username = "Test User";
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(username);
        assertEquals(username, authRequest.getUsername());
    }

    @Test
    void testGetSetPassword() {
        String password = "test password";
        AuthRequest authRequest = new AuthRequest();
        authRequest.setPassword(password);
        assertEquals(password, authRequest.getPassword());
    }
}
