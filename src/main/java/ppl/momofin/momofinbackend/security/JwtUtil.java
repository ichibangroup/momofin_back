package ppl.momofin.momofinbackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ppl.momofin.momofinbackend.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = System.getenv("SECRET_KEY");
    private static final long EXPIRATION_TIME = 1000L * 60 * 60;

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", new ArrayList<>(user.getRoles()));  // Convert Set to List
        claims.put("organizationId", user.getOrganization().getOrganizationId());
        claims.put("isOrganizationAdmin", user.isOrganizationAdmin());
        return createToken(claims, user.getUsername());
    }

    public String generateToken(String username) {
        return createToken(new HashMap<>(), username);
    }

    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username));
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractOrganizationId(String token) {
        return extractClaim(token, claims -> claims.get("organizationId", Long.class));
    }

    public Boolean extractIsOrganizationAdmin(String token) {
        return extractClaim(token, claims -> claims.get("isOrganizationAdmin", Boolean.class));
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME * 1000))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY).compact();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}