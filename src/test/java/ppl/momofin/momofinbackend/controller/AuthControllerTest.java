package ppl.momofin.momofinbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.slf4j.Logger;
import ppl.momofin.momofinbackend.config.SecurityConfig;
import ppl.momofin.momofinbackend.error.InvalidCredentialsException;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.service.UserService;
import ppl.momofin.momofinbackend.request.AuthRequest;
import ppl.momofin.momofinbackend.utility.JwtUtil;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private Logger logger;

    private User mockUser;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockUser = new User();
        mockUser.setUsername("test User");
        mockUser.setPassword("testPassword");
    }

    @Test
    void testAuthenticateUserSuccess() throws Exception {
        // Mock UserService's authenticate method
        when(userService.authenticate(anyString(), anyString(), anyString())).thenReturn(mockUser);

        // Mock JwtUtil's generateToken method
        when(jwtUtil.generateToken(anyString())).thenReturn("mock-jwt-token");

        // Create an authentication request object
        AuthRequest authRequest = new AuthRequest();
        authRequest.setOrganizationName("My Organization");
        authRequest.setUsername("test User");
        authRequest.setPassword("testPassword");

        // Perform the POST request to /auth/login
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk()) // Assert that the status is 200 OK
                .andExpect(jsonPath("$.jwt").value("mock-jwt-token")); // Assert that the JWT token is in the response
    }

    @Test
    void testAuthenticateUserInvalidCredentials() throws Exception {
        when(userService.authenticate(anyString(), anyString(), anyString()))
                .thenThrow(new InvalidCredentialsException());

        AuthRequest authRequest = new AuthRequest();
        authRequest.setOrganizationName("My Organization");
        authRequest.setUsername("Hobo Steve Invalid");
        authRequest.setPassword("wrongPassword");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorMessage").value("Your email or password is incorrect"));
    }

    @Test
    void testAuthenticateUserLoggingSuccess() throws Exception {
        when(userService.authenticate(anyString(), anyString(), anyString())).thenReturn(mockUser);
        when(jwtUtil.generateToken(anyString())).thenReturn("mock-jwt-token");

        AuthRequest authRequest = new AuthRequest();
        authRequest.setOrganizationName("My Organization");
        authRequest.setUsername("test user");
        authRequest.setPassword("test password");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").value("mock-jwt-token"));

        verify(logger).info("Successful login for user: {} from organization: {}", "test User", "My Organization");
    }

    @Test
    void testAuthenticateUserLoggingFailure() throws Exception {
        when(userService.authenticate(anyString(), anyString(), anyString()))
                .thenThrow(new InvalidCredentialsException());

        AuthRequest authRequest = new AuthRequest();
        authRequest.setOrganizationName("My Organization");
        authRequest.setUsername("Hobo Steve Invalid");
        authRequest.setPassword("wrongPassword");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorMessage").value("Your email or password is incorrect"));

        // Verify that the failed login attempt was logged
        verify(logger).warn("Failed login attempt for user: {} from organization: {}", "Hobo Steve Invalid", "My Organization");
    }
}
