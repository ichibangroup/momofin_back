package ppl.momofin.momofinbackend.response;

import org.junit.jupiter.api.Test;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.model.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FetchAllUserResponseTest {
    @Test
    void testFetchAllUserResponseConstructorEmpty() {
        FetchAllUserResponse userResponse = new FetchAllUserResponse();

        assertNotNull(userResponse);
        assertNull(userResponse.getUserId());
        assertNull(userResponse.getOrganization());
        assertNull(userResponse.getUsername());
        assertNull(userResponse.getName());
        assertNull(userResponse.getEmail());
    }

    @Test
    void testFetchAllUserResponseConstructor() {
        Long userId = 1L;
        String organization = "testOrganization";
        String username = "Manager_Test";
        String name = "testname";
        String email = "test@example.com";

        FetchAllUserResponse userResponse = new FetchAllUserResponse(userId, username, name, email, organization);

        assertNotNull(userResponse);
        assertEquals(userId, userResponse.getUserId());
        assertEquals(organization, userResponse.getOrganization());
        assertEquals(username, userResponse.getUsername());
        assertEquals(name, userResponse.getName());
        assertEquals(email, userResponse.getEmail());
    }

    @Test
    void testGetSetUserId() {
        Long userId = 1L;
        FetchAllUserResponse userResponse = new FetchAllUserResponse();

        userResponse.setUserId(userId);
        assertEquals(userId, userResponse.getUserId());
    }

    @Test
    void  testGetSetOrganization() {
        String organization = "testOrganization";
        FetchAllUserResponse userResponse = new FetchAllUserResponse();

        userResponse.setOrganization(organization);
        assertEquals(organization, userResponse.getOrganization());
    }
    @Test
    void testGetSetUsername() {
        String username = "testUser";
        FetchAllUserResponse userResponse = new FetchAllUserResponse();

        userResponse.setUsername(username);
        assertEquals(username, userResponse.getUsername());
    }


    @Test
    void testGetSetName() {
        String name = "testName";
        FetchAllUserResponse userResponse = new FetchAllUserResponse();

        userResponse.setName(name);
        assertEquals(name, userResponse.getName());
    }

    @Test
    void testGetSetEmail() {
        String email = "test@email.com";
        FetchAllUserResponse userResponse = new FetchAllUserResponse();

        userResponse.setEmail(email);
        assertEquals(email, userResponse.getEmail());
    }

    @Test
    void testFromUser() {
        String organizationName = "test org name";
        Organization organization = new Organization();
        organization.setName(organizationName);
        String username = "Manager Test";
        String name = "testname";
        String email = "test@example.com";
        String password = "testpassword";
        String position = "Manager";

        User user = new User(organization, username, name, email, password, position);

        FetchAllUserResponse userResponse = FetchAllUserResponse.fromUser(user);

        assertNotNull(userResponse);
        assertNull(userResponse.getUserId());
        assertEquals(organizationName, userResponse.getOrganization());
        assertEquals(username, userResponse.getUsername());
        assertEquals(name, userResponse.getName());
        assertEquals(email, userResponse.getEmail());
    }
}
