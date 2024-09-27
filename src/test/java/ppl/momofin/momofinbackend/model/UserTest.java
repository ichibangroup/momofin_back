package ppl.momofin.momofinbackend.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;



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

        User user = new User(organization, username, name, email, password, position, false, true);

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
        Long userId = 1L;
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

        user.setName(username);
        assertEquals(username, user.getName());
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
}

