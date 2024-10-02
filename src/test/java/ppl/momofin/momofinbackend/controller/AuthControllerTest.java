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
import org.springframework.test.web.servlet.MvcResult;
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
import ppl.momofin.momofinbackend.security.JwtUtil;

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
    public void testAuthenticateUserSuccess() throws Exception {
        Organization mockOrg = new Organization("My Organization", "Test Description");
        mockOrg.setOrganizationId(1L);

        User mockUser = new User(
                mockOrg,
                "test User",
                "Test User Full Name",
                "test.user@example.com",
                "testPassword",
                "Tester",
                false
        );
        mockUser.setUserId(1L);

        when(userService.authenticate(anyString(), anyString(), anyString())).thenReturn(mockUser);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("mock-jwt-token");

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizationName\":\"My Organization\",\"username\":\"test User\",\"password\":\"testPassword\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println("Response content: " + content);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizationName\":\"My Organization\",\"username\":\"test User\",\"password\":\"testPassword\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").value("mock-jwt-token"))
                .andExpect(jsonPath("$.user.userId").value(1))
                .andExpect(jsonPath("$.user.username").value("test User"))
                .andExpect(jsonPath("$.user.name").value("Test User Full Name"))
                .andExpect(jsonPath("$.user.email").value("test.user@example.com"))
                .andExpect(jsonPath("$.user.position").value("Tester"))
                .andExpect(jsonPath("$.user.organizationAdmin").value(false))
                .andExpect(jsonPath("$.user.organization.name").value("My Organization"));
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
