package ppl.momofin.momofinbackend.utility;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import ppl.momofin.momofinbackend.security.JwtUtil;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    private String token;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        token = jwtUtil.generateToken("Test User");
    }

    @Test
    void testGenerateToken() {
        assertNotNull(token);
    }

    @Test
    void testValidateTokenSuccess() {
        assertTrue(jwtUtil.validateToken(token, "Test User"));
    }

    @Test
    void testValidateTokenFailure() {
        assertFalse(jwtUtil.validateToken(token, "Wrong User"));
    }


    @Test
    void testIsTokenExpired() {
        String shortLivedToken = Jwts.builder()
                .setSubject("Test User")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000)) // 1 second
                .signWith(SignatureAlgorithm.HS256, System.getenv("SECRET_KEY"))
                .compact();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertThrows(ExpiredJwtException.class,
                () -> jwtUtil.validateToken(shortLivedToken, "Test User"));
    }
}

