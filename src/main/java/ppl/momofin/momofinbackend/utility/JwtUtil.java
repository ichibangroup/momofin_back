package ppl.momofin.momofinbackend.utility;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    public String generateToken(String email) {
        return null;
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return null;
    }

    public boolean validateToken(String token, String email) {
        return false;
    }

    private boolean isTokenExpired(String token) {
        return false;
    }

    private Date extractExpiration(String token) {
        return null;
    }

    private String extractEmail(String token) {
        return null;
    }
}
