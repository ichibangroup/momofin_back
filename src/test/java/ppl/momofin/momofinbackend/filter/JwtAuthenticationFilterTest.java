package ppl.momofin.momofinbackend.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ppl.momofin.momofinbackend.security.JwtUtil;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Claims claims;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_WithValidToken_ShouldSetAuthentication() throws Exception {
        String token = "valid_token";
        String username = "testuser";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(jwtUtil.validateToken(eq(token), eq(username))).thenReturn(true);
        when(jwtUtil.extractAllClaims(token)).thenReturn(claims);
        when(claims.get("roles")).thenReturn(Collections.singletonList("ROLE_USER"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtUtil).extractUsername(token);
        verify(jwtUtil).validateToken(token, username);
        verify(filterChain).doFilter(request, response);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(1, auth.getAuthorities().size());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void doFilterInternal_WithNoAuthorizationHeader_ShouldNotSetAuthentication() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_WithInvalidTokenFormat_ShouldNotSetAuthentication() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("InvalidToken");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_WithInvalidToken_ShouldNotSetAuthentication() throws Exception {
        String token = "invalid_token";
        String username = "testuser";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(jwtUtil.validateToken(eq(token), eq(username))).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_WithValidTokenButNoRoles_ShouldSetAuthenticationWithDefaultRole() throws Exception {
        String token = "valid_token";
        String username = "testuser";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(jwtUtil.validateToken(eq(token), eq(username))).thenReturn(true);
        when(jwtUtil.extractAllClaims(token)).thenReturn(claims);
        when(claims.get("roles")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(1, auth.getAuthorities().size());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void doFilterInternal_WithValidTokenAndNonListRoles_ShouldNotSetAuthority() throws Exception {
        String token = "valid_token";
        String username = "testuser";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(jwtUtil.validateToken(eq(token), eq(username))).thenReturn(true);
        when(jwtUtil.extractAllClaims(token)).thenReturn(claims);
        when(claims.get("roles")).thenReturn("NOT_A_LIST");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().isEmpty());
    }

    @Test
    void doFilterInternal_WithValidTokenAndEmptyRolesList_ShouldNotSetAuthority() throws Exception {
        String token = "valid_token";
        String username = "testuser";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(jwtUtil.validateToken(eq(token), eq(username))).thenReturn(true);
        when(jwtUtil.extractAllClaims(token)).thenReturn(claims);
        when(claims.get("roles")).thenReturn(Collections.emptyList());

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().isEmpty());
    }

    @Test
    void doFilterInternal_WithValidTokenButAuthenticationAlreadySet_ShouldNotChangeAuthentication() throws Exception {
        String token = "valid_token";
        String username = "testuser";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);

        // Set up a mock authentication
        Authentication mockAuth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(mockAuth);
        SecurityContextHolder.setContext(securityContext);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        // Ensure that the authentication wasn't changed
        assertSame(mockAuth, SecurityContextHolder.getContext().getAuthentication());
        // Ensure that validateToken was never called
        verify(jwtUtil, never()).validateToken(any(), any());
    }
}