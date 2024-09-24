package ppl.momofin.momofinbackend.model;

import static org.junit.jupiter.api.Assertions.*;

import model.Organization;
import model.User;
import org.junit.jupiter.api.Test;



public class UserTest {
    @Test
    public void testUserContructorEmpty() {
        User user = new User();

        assertNotNull(user);
        assertNull(user.getUserId());
        assertNull(user.getOrganization());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getPosition());
        assertFalse(user.isOrganizationAdmin());
        assertFalse(user.isMomofinAdmin());
    }

    @Test
    public void testUserConstructorRolesNotSpecified() {
        Organization organization = new Organization();
        String name = "testname";
        String email = "test@example.com";
        String password = "testpassword";
        String position = "Manager";

        User user = new User(organization, name, email, password, position);

        assertNotNull(user);
        assertNull(user.getUserId());
        assertEquals(organization, user.getOrganization());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(position, user.getPosition());
        assertFalse(user.isOrganizationAdmin());
        assertFalse(user.isMomofinAdmin());
    }

    @Test
    public void testUserConstructorOrganizationAdmin() {
        Organization organization = new Organization();
        String name = "testname";
        String email = "test@example.com";
        String password = "testpassword";
        String position = "Manager";

        User user = new User(organization, name, email, password, position, true);

        assertNotNull(user);
        assertNull(user.getUserId());
        assertEquals(organization, user.getOrganization());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(position, user.getPosition());
        assertTrue(user.isOrganizationAdmin());
        assertFalse(user.isMomofinAdmin());
    }

    @Test
    public void testUserConstructorMomofinAdmin() {
        Organization organization = new Organization();
        String name = "testname";
        String email = "test@example.com";
        String password = "testpassword";
        String position = "Manager";

        User user = new User(organization, name, email, password, position, false, true);

        assertNotNull(user);
        assertNull(user.getUserId());
        assertEquals(organization, user.getOrganization());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(position, user.getPosition());
        assertFalse(user.isOrganizationAdmin());
        assertTrue(user.isMomofinAdmin());
    }

    @Test
    public void testGetSetUserId() {
        Long userId = 1L;
        User user = new User();

        user.setUserId(userId);
        assertEquals(userId, user.getUserId());
    }

    @Test
    public void  testGetSetOrganization() {
        Organization organization = new Organization();
        User user = new User();

        user.setOrganization(organization);
        assertEquals(organization, user.getOrganization());
    }

    @Test
    public void testGetSetName() {
        String name = "testName";
        User user = new User();

        user.setName(name);
        assertEquals(name, user.getName());
    }

    @Test
    public void testGetSetEmail() {
        String email = "test@email.com";
        User user = new User();

        user.setEmail(email);
        assertEquals(email, user.getEmail());
    }

    @Test
    public void testGetSetPassword() {
        String password = "test password";
        User user = new User();

        user.setPassword(password);
        assertEquals(password, user.getPassword());
    }

    @Test
    public void testGetSetPosition() {
        String position = "Manager";
        User user = new User();

        user.setPosition(position);
        assertEquals(position, user.getPosition());
    }

    @Test
    public void testGetSetIsOrganizationAdmin() {
        User user = new User();

        user.setOrganizationAdmin(true);
        assertTrue(user.isOrganizationAdmin());
    }

    @Test
    public void testGetSetIsMomofinAdmin() {
        User user = new User();

        user.setMomofinAdmin(true);
        assertTrue(user.isMomofinAdmin());
    }
}

