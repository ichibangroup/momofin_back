package ppl.momofin.momofinbackend.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ppl.momofin.momofinbackend.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = jwtUtil.extractUsername(jwt);
        }

        logger.debug("JWT: {}", jwt);
        logger.debug("Extracted username: {}", username);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validateToken(jwt, username)) {
                Claims claims = jwtUtil.extractAllClaims(jwt);
                logger.debug("All claims: {}", claims);

                List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                // Check if 'roles' claim exists
                if (claims.get("roles") != null) {
                    Object rolesObj = claims.get("roles");
                    if (rolesObj instanceof List) {
                        List<?> rolesList = (List<?>) rolesObj;
                        authorities = rolesList.stream()
                                .filter(role -> role instanceof String)
                                .map(role -> new SimpleGrantedAuthority((String) role))
                                .collect(Collectors.toList());
                        logger.debug("Extracted roles: {}", authorities);
                    } else {
                        logger.warn("'roles' claim is not a List: {}", rolesObj);
                    }
                } else {
                    logger.warn("No 'roles' claim found in the token");

                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                }

                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        username, null, authorities);
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                logger.debug("Authentication set in SecurityContext for user: {} with authorities: {}", username, authorities);
            } else {
                logger.debug("Token validation failed for user: {}", username);
            }
        }

        chain.doFilter(request, response);
    }
}