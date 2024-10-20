package ppl.momofin.momofinbackend.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RolesTest {
    @Test
    void testEmptyConstructor() {
        Roles roles = new Roles();
        assertNotNull(roles);
        assertFalse(roles.isOrganizationalAdmin());
        assertFalse(roles.isMomofinAdmin());
    }

    @Test
    void testConstructor() {
        Roles roles = new Roles(true, true);
        assertNotNull(roles);
        assertTrue(roles.isOrganizationalAdmin());
        assertTrue(roles.isMomofinAdmin());
    }

    @Test
    void testGetSetIsOrganizationalAdmin() {
        Roles roles = new Roles();
        roles.setOrganizationalAdmin(true);
        assertNotNull(roles);
        assertTrue(roles.isOrganizationalAdmin());
        assertFalse(roles.isMomofinAdmin());
    }

    @Test
    void testGetSetIsMomofinAdmin() {
        Roles roles = new Roles();
        roles.setMomofinAdmin(true);
        assertNotNull(roles);
        assertFalse(roles.isOrganizationalAdmin());
        assertTrue(roles.isMomofinAdmin());
    }
}
