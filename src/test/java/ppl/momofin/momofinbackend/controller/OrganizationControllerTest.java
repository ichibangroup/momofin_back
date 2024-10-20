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
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.service.OrganizationService;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrganizationControllerTest {

    private MockMvc mockMvc;

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
        mockMvc = MockMvcBuilders.standaloneSetup(organizationController).build();

        testOrg = new Organization("Test Org", "Test Description");
        testOrg.setOrganizationId(1L);
        testUser = new User(testOrg, "testuser", "Test User", "test@example.com", "password", "Developer", false);
        testUserDTO = new UserDTO(testUser.getUserId(), testUser.getUsername(), testUser.getName(), testUser.getEmail(), testUser.getPosition(), testUser.isOrganizationAdmin());
    }

    @Test
    void updateOrganization_ShouldReturnUpdatedOrganization() throws Exception {
        when(organizationService.updateOrganization(eq(1L), anyString(), anyString())).thenReturn(testOrg);

        mockMvc.perform(put("/api/organizations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Org\",\"description\":\"Updated Description\"}"))
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
        mockMvc.perform(delete("/api/organizations/1/users/1"))
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
}