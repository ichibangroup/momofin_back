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
        testOrg.setOrganizationId(1L);
        testUser = new User(testOrg, "testuser", "Test User", "test@example.com",
                "password", "Developer", false);
        testUserDTO = new UserDTO(testUser.getUserId(), testUser.getUsername(),
                testUser.getName(), testUser.getEmail(), testUser.getPosition(),
                testUser.isOrganizationAdmin(), testUser.isMomofinAdmin());
    }

    @Test
    void updateOrganization_ShouldReturnUpdatedOrganization() throws Exception {
        when(organizationService.updateOrganization(eq(1L), anyString(), anyString(), anyString(), anyString())).thenReturn(testOrg);

        mockMvc.perform(put("/api/organizations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Org\",\"description\":\"Updated Description\",\"industry\":\"Updated Industry\",\"location\":\"Updated Location\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Org"))
                .andExpect(jsonPath("$.description").value("Test Description"));
    }

    @Test
    void getUsersInOrganization_ShouldReturnListOfUsers() throws Exception {
        when(organizationService.getUsersInOrganization(1L)).thenReturn(Arrays.asList(testUserDTO));

        mockMvc.perform(get("/api/organizations/1/users"))
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

        mockMvc.perform(delete("/api/organizations/1/users/1")
                        .header("Authorization", token))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateUserInOrganization_ShouldReturnUpdatedUser() throws Exception {
        when(organizationService.updateUserInOrganization(eq(1L), eq(1L), any(UserDTO.class))).thenReturn(testUserDTO);

        mockMvc.perform(put("/api/organizations/1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"updateduser\",\"name\":\"Updated User\",\"email\":\"updated@example.com\",\"position\":\"Senior Developer\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void getOrganizationTest() throws Exception {
        when(organizationService.findOrganizationById(1L)).thenReturn(testOrg);

        mockMvc.perform(get("/api/organizations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Org"))
                .andExpect(jsonPath("$.description").value("Test Description"));

        verify(organizationService).findOrganizationById(1L);
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
        mockMvc.perform(delete("/api/organizations/1/users/23")
                        .header("Authorization", token))
                .andExpect(status().isNoContent());

        verify(organizationService).deleteUser(1L, 23L, adminUser);
    }

    @Test
    void deleteUser_ReturnsForbidden_WhenNotAuthorized() throws Exception {
        // Setup
        String token = "Bearer valid_token";
        when(jwtUtil.extractUsername(anyString())).thenReturn("user");
        when(userService.fetchUserByUsername("user")).thenReturn(new User());
        doThrow(new UserDeletionException("Not authorized"))
                .when(organizationService).deleteUser(anyLong(), anyLong(), any(User.class));

        // Execute & Verify
        mockMvc.perform(delete("/api/organizations/1/users/23")
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
                .when(organizationService).deleteUser(anyLong(), anyLong(), any(User.class));

        // Execute & Verify
        mockMvc.perform(delete("/api/organizations/1/users/23")
                        .header("Authorization", token))
                .andExpect(status().isNotFound());
    }

}