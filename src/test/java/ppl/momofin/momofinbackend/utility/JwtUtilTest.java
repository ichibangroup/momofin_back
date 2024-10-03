package ppl.momofin.momofinbackend.utility;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.security.JwtUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private User testUser;
    private String secretKey;

    @BeforeEach
    void setUp() throws Exception {
        jwtUtil = new JwtUtil();
        Organization org = new Organization("Test Org");
        org.setOrganizationId(1L);
        testUser = new User(org, "testuser", "Test User", "test@example.com", "password", "Developer", true);

        // Use reflection to get the SECRET_KEY
        Field secretKeyField = JwtUtil.class.getDeclaredField("SECRET_KEY");
        secretKeyField.setAccessible(true);
        secretKey = (String) secretKeyField.get(null);
    }

    @Test
    void generateToken_WithUser_ShouldIncludeOrganizationAndAdminInfo() {
        String token = jwtUtil.generateToken(testUser);

        assertTrue(jwtUtil.validateToken(token));
        assertEquals(testUser.getUsername(), jwtUtil.extractUsername(token));
        assertEquals(testUser.getOrganization().getOrganizationId(), jwtUtil.extractOrganizationId(token));
        assertTrue(jwtUtil.extractIsOrganizationAdmin(token));
    }

    @Test
    void generateToken_WithUsername_ShouldCreateValidToken() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        assertTrue(jwtUtil.validateToken(token));
        assertEquals(username, jwtUtil.extractUsername(token));
    }

    @Test
    void validateToken_WithValidTokenAndUsername_ShouldReturnTrue() {
        String token = jwtUtil.generateToken(testUser);
        assertTrue(jwtUtil.validateToken(token, testUser.getUsername()));
    }

    @Test
    void validateToken_WithInvalidUsername_ShouldReturnFalse() {
        String token = jwtUtil.generateToken(testUser);
        assertFalse(jwtUtil.validateToken(token, "wrongusername"));
    }

    @Test
    void validateToken_WithExpiredTokenAndValidUsername_ShouldReturnFalseOrThrowException() {
        // Create an expired token
        String token = Jwts.builder()
                .setClaims(new HashMap<>())
                .setSubject(testUser.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis() - 2000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        try {
            boolean result = jwtUtil.validateToken(token, testUser.getUsername());
            assertFalse(result, "Expected validateToken to return false for an expired token");
        } catch (ExpiredJwtException e) {
            // If an ExpiredJwtException is thrown, we consider this a valid way to indicate an expired token
            assertTrue(true, "ExpiredJwtException was thrown, which is acceptable for an expired token");
        }
    }

    @Test
    void validateToken_WithValidTokenAndInvalidUsername_ShouldReturnFalse() {
        String token = jwtUtil.generateToken(testUser);
        assertFalse(jwtUtil.validateToken(token, "invalidusername"));
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        assertFalse(jwtUtil.validateToken("invalidtoken"));
    }

    @Test
    void extractAllClaims_WithValidToken_ShouldReturnClaims() {
        String token = jwtUtil.generateToken(testUser);
        Claims claims = jwtUtil.extractAllClaims(token);
        assertNotNull(claims);
        assertEquals(testUser.getUsername(), claims.getSubject());
    }

    @Test
    void extractAllClaims_WithInvalidToken_ShouldThrowException() {
        assertThrows(Exception.class, () -> jwtUtil.extractAllClaims("invalidtoken"));
    }

    @Test
    void extractUsername_WithValidToken_ShouldReturnUsername() {
        String token = jwtUtil.generateToken(testUser);
        assertEquals(testUser.getUsername(), jwtUtil.extractUsername(token));
    }

    @Test
    void extractOrganizationId_WithValidToken_ShouldReturnOrganizationId() {
        String token = jwtUtil.generateToken(testUser);
        assertEquals(testUser.getOrganization().getOrganizationId(), jwtUtil.extractOrganizationId(token));
    }

    @Test
    void extractIsOrganizationAdmin_WithValidToken_ShouldReturnIsOrganizationAdmin() {
        String token = jwtUtil.generateToken(testUser);
        assertTrue(jwtUtil.extractIsOrganizationAdmin(token));
    }

    @Test
    void isTokenExpired_WithExpiredToken_ShouldReturnTrueOrThrowException() throws Exception {
        // Create an expired token
        String token = Jwts.builder()
                .setClaims(new HashMap<>())
                .setSubject(testUser.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis() - 2000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        // Use reflection to call the private isTokenExpired method
        Method isTokenExpiredMethod = JwtUtil.class.getDeclaredMethod("isTokenExpired", String.class);
        isTokenExpiredMethod.setAccessible(true);

        try {
            boolean result = (Boolean) isTokenExpiredMethod.invoke(jwtUtil, token);
            assertTrue(result, "Expected isTokenExpired to return true for an expired token");
        } catch (Exception e) {
            if (e.getCause() instanceof ExpiredJwtException) {
                // If an ExpiredJwtException is thrown, we consider this a valid way to indicate an expired token
                assertTrue(true, "ExpiredJwtException was thrown, which is acceptable for an expired token");
            } else {
                // If it's any other kind of exception, we rethrow it
                throw e;
            }
        }
    }

    @Test
    void isTokenExpired_WithValidToken_ShouldReturnFalse() throws Exception {
        String token = jwtUtil.generateToken(testUser);

        // Use reflection to call the private isTokenExpired method
        Method isTokenExpiredMethod = JwtUtil.class.getDeclaredMethod("isTokenExpired", String.class);
        isTokenExpiredMethod.setAccessible(true);
        assertFalse((Boolean) isTokenExpiredMethod.invoke(jwtUtil, token));
    }
    @Test
    void validateToken_WithExpiredTokenAndMatchingUsername_ShouldReturnFalse() {
        // Create an expired token
        String expiredToken = Jwts.builder()
                .setClaims(new HashMap<>())
                .setSubject(testUser.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis() - 2000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        try {
            boolean result = jwtUtil.validateToken(expiredToken, testUser.getUsername());
            assertFalse(result, "Expected validateToken to return false for an expired token, even with matching username");
        } catch (ExpiredJwtException e) {
            // If an ExpiredJwtException is thrown, we consider this equivalent to returning false
            assertTrue(true, "ExpiredJwtException was thrown, which is equivalent to returning false for an expired token");
        }
    }
    @Test
    void validateToken_ComprehensiveTest() {
        // Case 1: Valid token and matching username (true && true)
        String validToken = jwtUtil.generateToken(testUser);
        assertTrue(jwtUtil.validateToken(validToken, testUser.getUsername()),
                "Should return true for valid token and matching username");

        // Case 2: Valid token but non-matching username (false && true)
        assertFalse(jwtUtil.validateToken(validToken, "wrongUsername"),
                "Should return false for valid token but non-matching username");

        // Case 3: Expired token and matching username (true && false)
        String expiredToken = Jwts.builder()
                .setClaims(new HashMap<>())
                .setSubject(testUser.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis() - 2000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        try {
            assertFalse(jwtUtil.validateToken(expiredToken, testUser.getUsername()),
                    "Should return false for expired token, even with matching username");
        } catch (ExpiredJwtException e) {
            // If an ExpiredJwtException is thrown, we consider this equivalent to returning false
            assertTrue(true, "ExpiredJwtException was thrown, which is equivalent to returning false for an expired token");
        }

        // Case 4: Expired token and non-matching username (false && false)
        try {
            assertFalse(jwtUtil.validateToken(expiredToken, "wrongUsername"),
                    "Should return false for expired token and non-matching username");
        } catch (ExpiredJwtException e) {
            // If an ExpiredJwtException is thrown, we consider this equivalent to returning false
            assertTrue(true, "ExpiredJwtException was thrown, which is equivalent to returning false for an expired token");
        }
    }
}