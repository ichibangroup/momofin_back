package ppl.momofin.momofinbackend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ppl.momofin.momofinbackend.dto.UserDTO;
import ppl.momofin.momofinbackend.error.OrganizationNotFoundException;
import ppl.momofin.momofinbackend.error.UserDeletionException;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.security.JwtUtil;
import ppl.momofin.momofinbackend.service.OrganizationService;
import ppl.momofin.momofinbackend.service.UserService;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class OrganizationControllerTest {

    private MockMvc mockMvc;
    @Mock
    private UserService userService;  // Add this

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private OrganizationService organizationService;

    @InjectMocks
    private OrganizationController organizationController;

    private Organization testOrg;
    private User testUser;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(organizationController)
                .setControllerAdvice() // Add any exception handlers if you have them
                .build();

        testOrg = new Organization("Test Org", "Test Description");
        testOrg.setOrganizationId(UUID.fromString("ebe2e5c8-1434-4f91-a5f5-da690db03a6a"));
        testUser = new User(testOrg, "testuser", "Test User", "test@example.com",
                "password", "Developer", false);
        testUser.setUserId(UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe"));
        testUserDTO = new UserDTO(testUser.getUserId(), testUser.getUsername(),
                testUser.getName(), testUser.getEmail(), testUser.getPosition(),
                testUser.isOrganizationAdmin(), testUser.isMomofinAdmin());
    }

    @Test
    void updateOrganization_ShouldReturnUpdatedOrganization() throws Exception {
        when(organizationService.updateOrganization(eq(UUID.fromString("ebe2e5c8-1434-4f91-a5f5-da690db03a6a")), anyString(), anyString(), anyString(), anyString())).thenReturn(testOrg);

        mockMvc.perform(put("/api/organizations/ebe2e5c8-1434-4f91-a5f5-da690db03a6a")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Org\",\"description\":\"Updated Description\",\"industry\":\"Updated Industry\",\"location\":\"Updated Location\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Org"))
                .andExpect(jsonPath("$.description").value("Test Description"));
    }

    @Test
    void getUsersInOrganization_ShouldReturnListOfUsers() throws Exception {
        when(organizationService.getUsersInOrganization(UUID.fromString("ebe2e5c8-1434-4f91-a5f5-da690db03a6a"))).thenReturn(Arrays.asList(testUserDTO));

        mockMvc.perform(get("/api/organizations/ebe2e5c8-1434-4f91-a5f5-da690db03a6a/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    @Test
    void removeUserFromOrganization_ShouldReturnNoContent() throws Exception {
        String token = "Bearer valid_token";
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setOrganizationAdmin(true);

        when(jwtUtil.extractUsername(anyString())).thenReturn("admin");
        when(userService.fetchUserByUsername("admin")).thenReturn(adminUser);

        mockMvc.perform(delete("/api/organizations/ebe2e5c8-1434-4f91-a5f5-da690db03a6a/users/292aeace-0148-4a20-98bf-bf7f12871efe")
                        .header("Authorization", token))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateUserInOrganization_ShouldReturnUpdatedUser() throws Exception {
        when(organizationService.updateUserInOrganization(eq(UUID.fromString("ebe2e5c8-1434-4f91-a5f5-da690db03a6a")), eq(UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe")), any(UserDTO.class))).thenReturn(testUserDTO);

        mockMvc.perform(put("/api/organizations/ebe2e5c8-1434-4f91-a5f5-da690db03a6a/users/292aeace-0148-4a20-98bf-bf7f12871efe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"updateduser\",\"name\":\"Updated User\",\"email\":\"updated@example.com\",\"position\":\"Senior Developer\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void getOrganizationTest() throws Exception {
        when(organizationService.findOrganizationById(UUID.fromString("ebe2e5c8-1434-4f91-a5f5-da690db03a6a"))).thenReturn(testOrg);

        mockMvc.perform(get("/api/organizations/ebe2e5c8-1434-4f91-a5f5-da690db03a6a"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Org"))
                .andExpect(jsonPath("$.description").value("Test Description"));

        verify(organizationService).findOrganizationById(UUID.fromString("ebe2e5c8-1434-4f91-a5f5-da690db03a6a"));
    }
    @Test
    void deleteUser_Success() throws Exception {
        // Setup
        String token = "Bearer valid_token";
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setOrganizationAdmin(true);

        when(jwtUtil.extractUsername(anyString())).thenReturn("admin");
        when(userService.fetchUserByUsername("admin")).thenReturn(adminUser);

        // Execute & Verify
        mockMvc.perform(delete("/api/organizations/ebe2e5c8-1434-4f91-a5f5-da690db03a6a/users/292aeace-0148-4a20-98bf-bf7f12871efe")
                        .header("Authorization", token))
                .andExpect(status().isNoContent());

        verify(organizationService).deleteUser(UUID.fromString("ebe2e5c8-1434-4f91-a5f5-da690db03a6a"), UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe"), adminUser);
    }

    @Test
    void deleteUser_ReturnsForbidden_WhenNotAuthorized() throws Exception {
        // Setup
        String token = "Bearer valid_token";
        when(jwtUtil.extractUsername(anyString())).thenReturn("user");
        when(userService.fetchUserByUsername("user")).thenReturn(new User());
        doThrow(new UserDeletionException("Not authorized"))
                .when(organizationService).deleteUser(any(UUID.class), any(UUID.class), any(User.class));

        // Execute & Verify
        mockMvc.perform(delete("/api/organizations/ebe2e5c8-1434-4f91-a5f5-da690db03a6a/users/292aeace-0148-4a20-98bf-bf7f12871efe")
                        .header("Authorization", token))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_ReturnsNotFound_WhenOrganizationNotFound() throws Exception {
        // Setup
        String token = "Bearer valid_token";
        when(jwtUtil.extractUsername(anyString())).thenReturn("admin");
        when(userService.fetchUserByUsername("admin")).thenReturn(new User());
        doThrow(new OrganizationNotFoundException("Organization not found"))
                .when(organizationService).deleteUser(any(UUID.class), any(UUID.class), any(User.class));

        // Execute & Verify
        mockMvc.perform(delete("/api/organizations/ebe2e5c8-1434-4f91-a5f5-da690db03a6a/users/292aeace-0148-4a20-98bf-bf7f12871efe")
                        .header("Authorization", token))
                .andExpect(status().isNotFound());
    }

}