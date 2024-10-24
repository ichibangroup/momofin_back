package ppl.momofin.momofinbackend.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import ppl.momofin.momofinbackend.utility.Roles;

import java.util.Set;
import java.util.UUID;


class UserTest {
    @Test
    void testUserConstructorEmpty() {
        User user = new User();

        assertNotNull(user);
        assertNull(user.getUserId());
        assertNull(user.getOrganization());
        assertNull(user.getUsername());
        assertNull(user.getName());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getPosition());
        assertFalse(user.isOrganizationAdmin());
        assertFalse(user.isMomofinAdmin());
    }

    @Test
    void testUserConstructorRolesNotSpecified() {
        Organization organization = new Organization();
        String username = "Manager_Test";
        String name = "testname";
        String email = "test@example.com";
        String password = "testpassword";
        String position = "Manager";

        User user = new User(organization, username, name, email, password, position);

        assertNotNull(user);
        assertNull(user.getUserId());
        assertEquals(organization, user.getOrganization());
        assertEquals(username, user.getUsername());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(position, user.getPosition());
        assertFalse(user.isOrganizationAdmin());
        assertFalse(user.isMomofinAdmin());
    }

    @Test
    void testUserConstructorOrganizationAdmin() {
        Organization organization = new Organization();
        String username = "Manager Test";
        String name = "testname";
        String email = "test@example.com";
        String password = "testpassword";
        String position = "Manager";

        User user = new User(organization, username, name, email, password, position, true);

        assertNotNull(user);
        assertNull(user.getUserId());
        assertEquals(organization, user.getOrganization());
        assertEquals(username, user.getUsername());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(position, user.getPosition());
        assertTrue(user.isOrganizationAdmin());
        assertFalse(user.isMomofinAdmin());
    }

    @Test
    void testUserConstructorMomofinAdmin() {
        Organization organization = new Organization();
        String username = "Manager Test";
        String name = "testname";
        String email = "test@example.com";
        String password = "testpassword";
        String position = "Manager";

        User user = new User(organization, username, name, email, password, position, new Roles(false, true));

        assertNotNull(user);
        assertNull(user.getUserId());
        assertEquals(organization, user.getOrganization());
        assertEquals(username, user.getUsername());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(position, user.getPosition());
        assertFalse(user.isOrganizationAdmin());
        assertTrue(user.isMomofinAdmin());
    }

    @Test
    void testGetSetUserId() {
        UUID userId = UUID.fromString("ff354956-c4c4-4697-9814-e34cd5ef5d4b");
        User user = new User();

        user.setUserId(userId);
        assertEquals(userId, user.getUserId());
    }

    @Test
    void  testGetSetOrganization() {
        Organization organization = new Organization();
        User user = new User();

        user.setOrganization(organization);
        assertEquals(organization, user.getOrganization());
    }
    @Test
    void testGetSetUsername() {
        String username = "Position_Name or something";
        User user = new User();

        user.setUsername(username);
        assertEquals(username, user.getUsername());
    }


    @Test
    void testGetSetName() {
        String name = "testName";
        User user = new User();

        user.setName(name);
        assertEquals(name, user.getName());
    }

    @Test
    void testGetSetEmail() {
        String email = "test@email.com";
        User user = new User();

        user.setEmail(email);
        assertEquals(email, user.getEmail());
    }

    @Test
    void testGetSetPassword() {
        String password = "test password";
        User user = new User();

        user.setPassword(password);
        assertEquals(password, user.getPassword());
    }

    @Test
    void testGetSetPosition() {
        String position = "Manager";
        User user = new User();

        user.setPosition(position);
        assertEquals(position, user.getPosition());
    }

    @Test
    void testGetSetIsOrganizationAdmin() {
        User user = new User();

        user.setOrganizationAdmin(true);
        assertTrue(user.isOrganizationAdmin());
    }

    @Test
    void testGetSetIsMomofinAdmin() {
        User user = new User();

        user.setMomofinAdmin(true);
        assertTrue(user.isMomofinAdmin());
    }

    @Test
    void testGetRoles() {
        User regularUser = new User();
        regularUser.setOrganizationAdmin(false);
        regularUser.setMomofinAdmin(false);

        User orgAdmin = new User();
        orgAdmin.setOrganizationAdmin(true);
        orgAdmin.setMomofinAdmin(false);

        User momofinAdmin = new User();
        momofinAdmin.setOrganizationAdmin(false);
        momofinAdmin.setMomofinAdmin(true);

        User superAdmin = new User();
        superAdmin.setOrganizationAdmin(true);
        superAdmin.setMomofinAdmin(true);

        assertEquals(Set.of("ROLE_USER"), regularUser.getRoles());
        assertEquals(Set.of("ROLE_USER", "ROLE_ORG_ADMIN"), orgAdmin.getRoles());
        assertEquals(Set.of("ROLE_USER", "ROLE_MOMOFIN_ADMIN"), momofinAdmin.getRoles());
        assertEquals(Set.of("ROLE_USER", "ROLE_ORG_ADMIN", "ROLE_MOMOFIN_ADMIN"), superAdmin.getRoles());
    }
}

