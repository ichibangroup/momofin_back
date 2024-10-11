package ppl.momofin.momofinbackend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import ppl.momofin.momofinbackend.error.InvalidOrganizationException;
import ppl.momofin.momofinbackend.error.OrganizationNotFoundException;
import ppl.momofin.momofinbackend.error.UserAlreadyExistsException;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.request.AddOrganizationRequest;
import ppl.momofin.momofinbackend.service.OrganizationService;
import ppl.momofin.momofinbackend.response.OrganizationResponse;
import ppl.momofin.momofinbackend.service.UserService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import ppl.momofin.momofinbackend.model.User;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

class MomofinAdminControllerTest {

    @Mock
    private OrganizationService organizationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private MomofinAdminController momofinAdminController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllOrganizations_shouldReturnListOfOrganizations() {
        // Arrange
        Organization org1 = new Organization("Org1", "Description1");
        Organization org2 = new Organization("Org2", "Description2");
        List<Organization> organizations = Arrays.asList(org1, org2);

        when(organizationService.getAllOrganizations()).thenReturn(organizations);

        // Act
        ResponseEntity<List<OrganizationResponse>> response = momofinAdminController.getAllOrganizations();

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
        assertEquals("Org1", response.getBody().get(0).getName());
        assertEquals("Org2", response.getBody().get(1).getName());
    }

    @Test
    void addOrganization_shouldCreateNewOrganizationAndAdmin() {
        // Arrange
        AddOrganizationRequest request = new AddOrganizationRequest();
        request.setName("New Org");
        request.setDescription("New Description");
        request.setAdminUsername("admin");
        request.setAdminPassword("password");

        Organization newOrg = new Organization("New Org", "New Description");
        when(organizationService.createOrganization("New Org", "New Description")).thenReturn(newOrg);

        User adminUser = new User();
        when(userService.registerOrganizationAdmin(eq(newOrg), eq("admin"), eq("New Org Admin"), isNull(), eq("password"), isNull()))
                .thenReturn(adminUser);

        // Act
        ResponseEntity<OrganizationResponse> response = momofinAdminController.addOrganization(request);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("New Org", response.getBody().getName());
        assertEquals("New Description", response.getBody().getDescription());
        verify(userService).registerOrganizationAdmin(eq(newOrg), eq("admin"), eq("New Org Admin"), isNull(), eq("password"), isNull());
    }
    @Test
    void updateOrganization_shouldUpdateExistingOrganization() {
        // Arrange
        Long orgId = 1L;
        AddOrganizationRequest request = new AddOrganizationRequest();
        request.setName("Updated Org");
        request.setDescription("Updated Description");

        Organization updatedOrg = new Organization("Updated Org", "Updated Description");
        when(organizationService.updateOrganization(orgId, "Updated Org", "Updated Description")).thenReturn(updatedOrg);

        // Act
        ResponseEntity<OrganizationResponse> response = momofinAdminController.updateOrganization(orgId, request);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Updated Org", response.getBody().getName());
        assertEquals("Updated Description", response.getBody().getDescription());
    }

    @Test
    void addOrganization_shouldReturnErrorResponse_whenInvalidOrganizationException() {
        // Arrange
        AddOrganizationRequest request = new AddOrganizationRequest();
        request.setName("");  // Invalid name
        request.setDescription("Description");

        when(organizationService.createOrganization(anyString(), anyString()))
                .thenThrow(new InvalidOrganizationException("Organization name cannot be empty"));

        // Act
        ResponseEntity<?> response = momofinAdminController.addOrganization(request);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof OrganizationResponse);
        OrganizationResponse errorResponse = (OrganizationResponse) response.getBody();
        assertNull(errorResponse.getOrganizationId());
        assertEquals("Organization name cannot be empty", errorResponse.getErrorMessage());
        assertEquals("Description", errorResponse.getDescription());
        assertNull(errorResponse.getName());
    }
    @Test
    void addOrganization_shouldReturnErrorResponse_whenUserAlreadyExistsException() {
        // Arrange
        AddOrganizationRequest request = new AddOrganizationRequest();
        request.setName("New Org");
        request.setDescription("Description");
        request.setAdminUsername("existingAdmin");
        request.setAdminPassword("password");

        when(organizationService.createOrganization(anyString(), anyString()))
                .thenReturn(new Organization("New Org", "Description"));
        when(userService.registerOrganizationAdmin(any(), anyString(), anyString(), any(), anyString(), any()))
                .thenThrow(new UserAlreadyExistsException("Admin user already exists"));

        // Act
        ResponseEntity<OrganizationResponse> response = momofinAdminController.addOrganization(request);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Admin user already exists", response.getBody().getErrorMessage());
    }

    @Test
    void addOrganization_shouldReturnErrorResponse_whenUnexpectedException() {
        // Arrange
        AddOrganizationRequest request = new AddOrganizationRequest();
        request.setName("New Org");
        request.setDescription("Description");

        when(organizationService.createOrganization(anyString(), anyString()))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act
        ResponseEntity<OrganizationResponse> response = momofinAdminController.addOrganization(request);

        // Assert
        assertEquals(500, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred", response.getBody().getErrorMessage());
    }

    @Test
    void updateOrganization_shouldReturnNotFound_whenOrganizationNotFoundException() {
        // Arrange
        Long orgId = 1L;
        AddOrganizationRequest request = new AddOrganizationRequest();
        request.setName("Updated Org");
        request.setDescription("Updated Description");

        when(organizationService.updateOrganization(eq(orgId), anyString(), anyString()))
                .thenThrow(new OrganizationNotFoundException("Organization not found"));

        // Act
        ResponseEntity<OrganizationResponse> response = momofinAdminController.updateOrganization(orgId, request);

        // Assert
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void updateOrganization_shouldReturnBadRequest_whenInvalidOrganizationException() {
        // Arrange
        Long orgId = 1L;
        AddOrganizationRequest request = new AddOrganizationRequest();
        request.setName("");
        request.setDescription("Updated Description");

        when(organizationService.updateOrganization(eq(orgId), anyString(), anyString()))
                .thenThrow(new InvalidOrganizationException("Invalid organization name"));

        // Act
        ResponseEntity<OrganizationResponse> response = momofinAdminController.updateOrganization(orgId, request);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Invalid organization name", response.getBody().getErrorMessage());
    }

    @Test
    void updateOrganization_shouldReturnErrorResponse_whenUnexpectedException() {
        // Arrange
        Long orgId = 1L;
        AddOrganizationRequest request = new AddOrganizationRequest();
        request.setName("Updated Org");
        request.setDescription("Updated Description");

        when(organizationService.updateOrganization(eq(orgId), anyString(), anyString()))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act
        ResponseEntity<OrganizationResponse> response = momofinAdminController.updateOrganization(orgId, request);

        // Assert
        assertEquals(500, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred", response.getBody().getErrorMessage());
    }
}