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
import ppl.momofin.momofinbackend.error.OrganizationNotFoundException;
import ppl.momofin.momofinbackend.error.UserAlreadyExistsException;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.repository.OrganizationRepository;
import ppl.momofin.momofinbackend.request.RegisterRequest;
import ppl.momofin.momofinbackend.service.UserService;
import ppl.momofin.momofinbackend.request.AuthRequest;
import ppl.momofin.momofinbackend.utility.JwtUtil;

import java.util.Optional;

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
    private OrganizationRepository organizationRepository;

    @MockBean
    private Logger logger;

    private User mockUser;
    private ObjectMapper objectMapper;
    private Organization organization;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockUser = new User();
        mockUser.setName("test User real name");
        mockUser.setEmail("test.user@gmail.com");
        mockUser.setPosition("Tester");
        mockUser.setUsername("test User");
        mockUser.setPassword("testPassword");
        organization = new Organization("Momofin");
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
        String wrongPassword = "wrongPassword";
        when(userService.authenticate(anyString(), anyString(), eq(wrongPassword)))
                .thenThrow(new InvalidCredentialsException());

        AuthRequest authRequest = new AuthRequest();
        authRequest.setOrganizationName("My Organization");
        authRequest.setUsername("Hobo Steve Invalid");
        authRequest.setPassword(wrongPassword);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorMessage").value("Your email or password is incorrect"));
    }

    @Test
    void testAuthenticateUserOrganizationNotFound() throws Exception {
        String invalidOrganizationName = "Not Organization";
        when(userService.authenticate(eq(invalidOrganizationName), anyString(), anyString()))
                .thenThrow(new OrganizationNotFoundException(invalidOrganizationName));

        AuthRequest authRequest = new AuthRequest();
        authRequest.setOrganizationName(invalidOrganizationName);
        authRequest.setUsername("Hobo Steve Invalid");
        authRequest.setPassword("wrongPassword");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorMessage").value("The organization "+ invalidOrganizationName + " is not registered to our database"));
    }



    @Test
    void testRegisterUserEmailAlreadyInUse() throws Exception {
        String usedEmail = "duplicated.address@gmail.com";
        when(organizationRepository.findOrganizationByName("Momofin")).thenReturn(Optional.of(organization));
        when(userService.registerMember(eq(organization), anyString(), anyString(), eq(usedEmail), anyString(), anyString()))
                .thenThrow(new UserAlreadyExistsException("The email "+usedEmail+" is already in use"));

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("test User real name");
        registerRequest.setEmail(usedEmail);
        registerRequest.setPosition("Tester");
        registerRequest.setUsername("test User");
        registerRequest.setPassword("testPassword");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorMessage").value("The email "+usedEmail+" is already in use"));
    }

    @Test
    void testRegisterUserUsernameAlreadyInUse() throws Exception {
        String usedUsername = "Doppelganger";
        when(organizationRepository.findOrganizationByName("Momofin")).thenReturn(Optional.of(organization));
        when(userService.registerMember(eq(organization), eq(usedUsername), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new UserAlreadyExistsException("The username "+usedUsername+" is already in use"));

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("test User real name");
        registerRequest.setEmail("test.user@gmail.com");
        registerRequest.setPosition("Tester");
        registerRequest.setUsername(usedUsername);
        registerRequest.setPassword("testPassword");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorMessage").value("The username "+usedUsername+" is already in use"));
    }
}
