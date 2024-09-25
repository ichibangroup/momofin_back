package ppl.momofin.momofinbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ppl.momofin.momofinbackend.error.InvalidCredentialsException;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.service.UserService;
import ppl.momofin.momofinbackend.utility.AuthRequest;
import ppl.momofin.momofinbackend.utility.JwtUtil;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

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
        // Mock UserService's authenticate method to throw an InvalidCredentialsException
        when(userService.authenticate(anyString(), anyString(), anyString()))
                .thenThrow(new InvalidCredentialsException());

        // Create an authentication request object
        AuthRequest authRequest = new AuthRequest();
        authRequest.setOrganizationName("My Organization");
        authRequest.setUsername("Hobo Steve Invalid");
        authRequest.setPassword("wrongPassword");

        // Perform the POST request to /auth/login
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorMessage").value("Your email or password is incorrect"));
    }
}
