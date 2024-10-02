package ppl.momofin.momofinbackend.utility;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.model.User;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        Organization org = new Organization("Test Org");
        org.setOrganizationId(1L);
        testUser = new User(org, "testuser", "Test User", "test@example.com", "password", "Developer", true);
    }

    @Test
    void generateToken_ShouldIncludeOrganizationAndAdminInfo() {
        String token = jwtUtil.generateToken(testUser);

        assertTrue(jwtUtil.validateToken(token, testUser.getUsername()));
        assertEquals(testUser.getUsername(), jwtUtil.extractUsername(token));
        assertEquals(testUser.getOrganization().getOrganizationId(), jwtUtil.extractOrganizationId(token));
        assertTrue(jwtUtil.extractIsOrganizationAdmin(token));
    }
}
