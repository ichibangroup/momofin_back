package ppl.momofin.momofinbackend.controller;

import io.sentry.Sentry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import ppl.momofin.momofinbackend.error.*;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.repository.OrganizationRepository;
import ppl.momofin.momofinbackend.request.AddOrganizationRequest;
import ppl.momofin.momofinbackend.response.ErrorResponse;
import ppl.momofin.momofinbackend.response.FetchAllUserResponse;
import ppl.momofin.momofinbackend.response.Response;
import ppl.momofin.momofinbackend.service.OrganizationService;
import ppl.momofin.momofinbackend.response.OrganizationResponse;
import ppl.momofin.momofinbackend.service.UserService;
import ppl.momofin.momofinbackend.utility.Roles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MomofinAdminControllerTest {

    @Mock
    private OrganizationService organizationService;

    @Mock
    private UserService userService;
    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private MomofinAdminController momofinAdminController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(momofinAdminController, "organizationRepository", organizationRepository);
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
        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
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
        request.setIndustry("New Industry");
        request.setLocation("New Location");
        request.setAdminUsername("admin");
        request.setAdminPassword("password");

        Organization newOrg = new Organization("New Org", "New Description", "New Industry", "New Location");
        when(organizationService.createOrganization("New Org", "New Description", "New Industry", "New Location")).thenReturn(newOrg);

        User adminUser = new User();
        when(userService.registerOrganizationAdmin(eq(newOrg), eq("admin"), eq("New Org Admin"), isNull(), eq("password"), isNull()))
                .thenReturn(adminUser);

        // Act
        ResponseEntity<OrganizationResponse> response = momofinAdminController.addOrganization(request);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        assertEquals("New Org", response.getBody().getName());
        assertEquals("New Description", response.getBody().getDescription());
        assertEquals("New Industry", response.getBody().getIndustry());
        assertEquals("New Location", response.getBody().getLocation());
        verify(userService).registerOrganizationAdmin(eq(newOrg), eq("admin"), eq("New Org Admin"), isNull(), eq("password"), isNull());
    }

    @Test
    void updateOrganization_shouldUpdateExistingOrganization() {
        // Arrange
        String stringOrgId = "ebe2e5c8-1434-4f91-a5f5-da690db03a6a";
        UUID orgId = UUID.fromString(stringOrgId);
        AddOrganizationRequest request = new AddOrganizationRequest();
        request.setName("Updated Org");
        request.setDescription("Updated Description");
        request.setIndustry("Updated Industry");
        request.setLocation("Updated Location");

        Organization updatedOrg = new Organization("Updated Org", "Updated Description", "Updated Industry", "Updated Location");
        when(organizationService.updateOrganization(orgId, "Updated Org", "Updated Description", "Updated Industry", "Updated Location")).thenReturn(updatedOrg);

        // Act
        ResponseEntity<OrganizationResponse> response = momofinAdminController.updateOrganization(stringOrgId, request);

        // Assert
        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        assertEquals("Updated Org", response.getBody().getName());
        assertEquals("Updated Description", response.getBody().getDescription());
        assertEquals("Updated Industry", response.getBody().getIndustry());
        assertEquals("Updated Location", response.getBody().getLocation());
    }

    @Test
    void addOrganization_shouldReturnErrorResponse_whenInvalidOrganizationException() {
        // Arrange
        AddOrganizationRequest request = new AddOrganizationRequest();
        request.setName("");  // Invalid name
        request.setDescription("Description");
        request.setIndustry("Industry");
        request.setLocation("Location");

        when(organizationService.createOrganization(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new InvalidOrganizationException("Organization name cannot be empty"));

        // Act
        ResponseEntity<OrganizationResponse> response = momofinAdminController.addOrganization(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getOrganizationId());
        assertEquals("Organization name cannot be empty", response.getBody().getErrorMessage());
        assertEquals("Description", response.getBody().getDescription());
        assertNull(response.getBody().getName());
    }

    @Test
    void addOrganization_shouldReturnErrorResponse_whenUserAlreadyExistsException() {
        // Arrange
        AddOrganizationRequest request = new AddOrganizationRequest();
        request.setName("New Org");
        Organization org = new Organization();

        when(organizationService.createOrganization(any(), any(), any(), any()))
                .thenReturn(org);
        when(userService.registerOrganizationAdmin(eq(org), any(), any(), any(), any(), any()))
                .thenThrow(new UserAlreadyExistsException("Admin user already exists"));

        // Act
        ResponseEntity<OrganizationResponse> response = momofinAdminController.addOrganization(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Admin user already exists", response.getBody().getErrorMessage());
        verify(organizationRepository).delete(org);
    }

    @Test
    void addOrganization_shouldReturnErrorResponse_whenUnexpectedException() {
        // Arrange
        AddOrganizationRequest request = new AddOrganizationRequest();
        when(organizationService.createOrganization(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act
        ResponseEntity<OrganizationResponse> response = momofinAdminController.addOrganization(request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred: Unexpected error", response.getBody().getErrorMessage());
    }

    @Test
    void updateOrganization_shouldReturnNotFound_whenOrganizationNotFoundException() {
        // Arrange
        String stringOrgId = "ebe2e5c8-1434-4f91-a5f5-da690db03a6a";
        UUID orgId = UUID.fromString(stringOrgId);
        AddOrganizationRequest request = new AddOrganizationRequest();
        request.setName("Updated Org");
        request.setDescription("Updated Description");
        request.setLocation("Updated Location");
        request.setIndustry("Updated Industry");

        when(organizationService.updateOrganization(eq(orgId), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new OrganizationNotFoundException("Organization not found"));

        // Act
        ResponseEntity<OrganizationResponse> response = momofinAdminController.updateOrganization(stringOrgId, request);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode().value());
    }

    @Test
    void updateOrganization_shouldReturnBadRequest_whenInvalidOrganizationException() {
        // Arrange
        String stringOrgId = "ebe2e5c8-1434-4f91-a5f5-da690db03a6a";
        UUID orgId = UUID.fromString(stringOrgId);
        AddOrganizationRequest request = new AddOrganizationRequest();
        request.setName("");
        request.setDescription("Updated Description");
        request.setIndustry("Updated Industry");
        request.setLocation("Updated Location");

        when(organizationService.updateOrganization(eq(orgId), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new InvalidOrganizationException("Invalid organization name"));

        // Act
        ResponseEntity<OrganizationResponse> response = momofinAdminController.updateOrganization(stringOrgId, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Invalid organization name", response.getBody().getErrorMessage());
    }

    @Test
    void updateOrganization_shouldReturnErrorResponse_whenUnexpectedException() {
        // Arrange
        String stringOrgId = "ebe2e5c8-1434-4f91-a5f5-da690db03a6a";
        UUID orgId = UUID.fromString(stringOrgId);
        AddOrganizationRequest request = new AddOrganizationRequest();
        request.setName("Updated Org");
        request.setDescription("Updated Description");
        request.setIndustry("Updated Industry");
        request.setLocation("Updated Location");

        when(organizationService.updateOrganization(eq(orgId), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act
        ResponseEntity<OrganizationResponse> response = momofinAdminController.updateOrganization(stringOrgId, request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred", response.getBody().getErrorMessage());
    }

    @Test
    void fetchAllUsersTest() {
        List<User> users = getUsers();

        when(userService.fetchAllUsers()).thenReturn(users);

        ResponseEntity<List<FetchAllUserResponse>> response = momofinAdminController.getAllUsers();
        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        for (int i = 0; i<5; i++) {
            assertEquals(users.get(i).getUserId(), Objects.requireNonNull(response.getBody()).get(i).getUserId());
            assertEquals(users.get(i).getOrganization().getName(), Objects.requireNonNull(response.getBody()).get(i).getOrganization());
            assertEquals(users.get(i).getUsername(), Objects.requireNonNull(response.getBody()).get(i).getUsername());
            assertEquals(users.get(i).getName(), Objects.requireNonNull(response.getBody()).get(i).getName());
            assertEquals(users.get(i).getEmail(), Objects.requireNonNull(response.getBody()).get(i).getEmail());
        }
    }

    private static List<User> getUsers() {
        Organization momofin = new Organization("Momofin");
        User user1 = new User(momofin, "Momofin Financial Samuel","Samuel", "samuel@gmail.com", "encodedMy#Money9078", "Finance Manager");
        User user2 = new User(momofin, "Momofin CEO Darrel", "Darrel Hoei", "darellhoei@gmail.com", "encodedHisPassword#6768", "Co-Founder", true);
        User user3 = new User(momofin, "Momofin Admin Alex", "Alex", "alex@outlook.com", "encodedAlex&Password0959", "Admin", new Roles(true,true));

        List<User> users = new ArrayList<User>();
        users.add(user1);
        users.add(user2);
        users.add(user3);

        Organization otherOrganization = new Organization("Dondozo");
        User user4 = new User(otherOrganization, "Dondozo Intern Ron", "Ron", "temp-intern@yahoo.com", "encoded123456", "Intern");
        User user5 = new User(otherOrganization, "Dondozo Commander Tatsugiri","Tatsugiri", "commander@email.com", "encodedToxic%Mouth", "Commander", true);

        users.add(user4);
        users.add(user5);
        return users;
    }
    @Test
    void addOrganization_shouldReturnErrorResponse_whenSecurityValidationException() {
        // Arrange
        AddOrganizationRequest request = new AddOrganizationRequest();
        request.setName("SELECT * FROM users");  // SQL injection attempt
        request.setDescription("Description");

        when(organizationService.createOrganization(any(), any(), any(), any()))
                .thenThrow(new SecurityValidationException("SQL injection detected in input"));

        // Act
        ResponseEntity<OrganizationResponse> response = momofinAdminController.addOrganization(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getOrganizationId());
        assertEquals("SQL injection detected in input", response.getBody().getErrorMessage());
        assertEquals("Description", response.getBody().getDescription());
        assertNull(response.getBody().getName());
    }
    @Test
    void addOrganization_shouldLogSuccessMessage_whenCreateSuccessful() {
        // Arrange
        AddOrganizationRequest request = new AddOrganizationRequest();
        request.setName("New Org");
        request.setDescription("New Description");
        request.setIndustry("New Industry");
        request.setLocation("New Location");
        request.setAdminUsername("admin");
        request.setAdminPassword("password");

        Organization newOrg = new Organization("New Org", "New Description", "New Industry", "New Location");
        when(organizationService.createOrganization("New Org", "New Description", "New Industry", "New Location"))
                .thenReturn(newOrg);

        User adminUser = new User();
        when(userService.registerOrganizationAdmin(eq(newOrg), eq("admin"), eq("New Org Admin"), isNull(), eq("password"), isNull()))
                .thenReturn(adminUser);

        try (MockedStatic<Sentry> sentryMock = Mockito.mockStatic(Sentry.class)) {
            // Act
            ResponseEntity<OrganizationResponse> response = momofinAdminController.addOrganization(request);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            sentryMock.verify(() ->
                    Sentry.captureMessage(String.format(
                            "[Success] Organization created - Name: %s, Industry: %s, ID: %s",
                            request.getName(),
                            request.getIndustry(),
                            newOrg.getOrganizationId()
                    ))
            );
        }
    }

    @Test
    void addOrganization_shouldLogException_whenSecurityValidationOccurs() {
        // Arrange
        AddOrganizationRequest request = new AddOrganizationRequest();
        request.setName("SELECT * FROM users");
        request.setDescription("Description");

        SecurityValidationException exception = new SecurityValidationException("SQL injection detected in input");
        when(organizationService.createOrganization(any(), any(), any(), any()))
                .thenThrow(exception);

        try (MockedStatic<Sentry> sentryMock = Mockito.mockStatic(Sentry.class)) {
            // Act
            ResponseEntity<OrganizationResponse> response = momofinAdminController.addOrganization(request);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            sentryMock.verify(() -> Sentry.captureException(exception));
        }
    }

    @Test
    void updateOrganization_shouldLogSuccessMessage_whenUpdateSuccessful() {
        // Arrange
        String stringOrgId = "ebe2e5c8-1434-4f91-a5f5-da690db03a6a";
        UUID orgId = UUID.fromString(stringOrgId);
        AddOrganizationRequest request = new AddOrganizationRequest();
        request.setName("Updated Org");
        request.setDescription("Updated Description");
        request.setIndustry("Updated Industry");
        request.setLocation("Updated Location");

        Organization updatedOrg = new Organization("Updated Org", "Updated Description", "Updated Industry", "Updated Location");
        when(organizationService.updateOrganization(orgId, "Updated Org", "Updated Description", "Updated Industry", "Updated Location"))
                .thenReturn(updatedOrg);

        try (MockedStatic<Sentry> sentryMock = Mockito.mockStatic(Sentry.class)) {
            // Act
            ResponseEntity<OrganizationResponse> response = momofinAdminController.updateOrganization(stringOrgId, request);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            sentryMock.verify(() ->
                    Sentry.captureMessage(String.format(
                            "[Success] Organization updated - ID: %s, New Name: %s",
                            stringOrgId,
                            request.getName()
                    ))
            );
        }
    }

    @Test
    void updateOrganization_shouldLogException_whenSecurityValidationOccurs() {
        // Arrange
        String stringOrgId = "ebe2e5c8-1434-4f91-a5f5-da690db03a6a";
        UUID orgId = UUID.fromString(stringOrgId);
        AddOrganizationRequest request = new AddOrganizationRequest();
        request.setName("SELECT * FROM users");

        SecurityValidationException exception = new SecurityValidationException("SQL injection detected");
        when(organizationService.updateOrganization(eq(orgId), any(), any(), any(), any()))
                .thenThrow(exception);

        try (MockedStatic<Sentry> sentryMock = Mockito.mockStatic(Sentry.class)) {
            // Act
            ResponseEntity<OrganizationResponse> response = momofinAdminController.updateOrganization(stringOrgId, request);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            sentryMock.verify(() -> Sentry.captureException(exception));
        }
    }
    @Test
    void deleteOrganization_Success() {
        // Arrange
        String orgId = UUID.randomUUID().toString();

        // Act
        ResponseEntity<OrganizationResponse> response = momofinAdminController.deleteOrganization(orgId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(organizationService).deleteOrganization(UUID.fromString(orgId));
    }
    @Test
    void deleteOrganization_HandlesException() {
        // Arrange
        String orgId = UUID.randomUUID().toString();
        doThrow(new RuntimeException("Delete failed")).when(organizationService)
                .deleteOrganization(any(UUID.class));

        // Act
        ResponseEntity<OrganizationResponse> response = momofinAdminController.deleteOrganization(orgId);

        // Assert
        assertEquals(HttpStatus.GONE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getErrorMessage().contains("Delete failed"));
    }
    @Test
    void setOrganizationAdmin_Success() {
        // Arrange
        String orgId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        User updatedUser = new User();
        when(organizationService.setOrganizationAdmin(
                UUID.fromString(orgId),
                UUID.fromString(userId)
        )).thenReturn(updatedUser);

        // Act
        ResponseEntity<?> response = momofinAdminController.setOrganizationAdmin(orgId, userId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
    @Test
    void setOrganizationAdmin_HandlesSecurityException() {
        // Arrange
        String orgId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        when(organizationService.setOrganizationAdmin(any(), any()))
                .thenThrow(new SecurityException("Security violation"));

        // Act
        ResponseEntity<?> response = momofinAdminController.setOrganizationAdmin(orgId, userId);

        // Assert
        assertEquals(HttpStatus.GONE, response.getStatusCode());
        assertNotNull(response.getBody());
    }
    @Test
    void deleteOrganization_NotFound_ShouldReturnNotFound() {
        // Arrange
        String orgId = UUID.randomUUID().toString();
        doThrow(new OrganizationNotFoundException("Organization not found"))
                .when(organizationService).deleteOrganization(any(UUID.class));

        // Act
        ResponseEntity<OrganizationResponse> response = momofinAdminController.deleteOrganization(orgId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(organizationService).deleteOrganization(UUID.fromString(orgId));
    }

    @Test
    void setOrganizationAdmin_OrganizationNotFound_ShouldReturnNotFound() {
        // Arrange
        String orgId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();

        when(organizationService.setOrganizationAdmin(any(UUID.class), any(UUID.class)))
                .thenThrow(new OrganizationNotFoundException("Organization not found"));

        // Act
        ResponseEntity<Response> response = momofinAdminController.setOrganizationAdmin(orgId, userId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(organizationService).setOrganizationAdmin(UUID.fromString(orgId), UUID.fromString(userId));
    }

    @Test
    void setOrganizationAdmin_UnexpectedError_ShouldReturnGone() {
        // Arrange
        String orgId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();

        when(organizationService.setOrganizationAdmin(any(UUID.class), any(UUID.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act
        ResponseEntity<Response> response = momofinAdminController.setOrganizationAdmin(orgId, userId);

        // Assert
        assertEquals(HttpStatus.GONE, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertTrue(errorResponse.getErrorMessage().contains("Error setting organization admin"));
    }
    @Test
    void deleteUser_Success_AsMomofinAdmin() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        User requestingUser = new User();
        requestingUser.setMomofinAdmin(true);

        when(userService.fetchUserByUsername(anyString())).thenReturn(requestingUser);
        doNothing().when(organizationService).deleteUser(any(), any(UUID.class), any(User.class));

        try (MockedStatic<Sentry> sentryMock = Mockito.mockStatic(Sentry.class);
             MockedStatic<SecurityContextHolder> securityContextMock = Mockito.mockStatic(SecurityContextHolder.class)) {

            // Mock security context
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            when(authentication.getName()).thenReturn("admin");
            when(securityContext.getAuthentication()).thenReturn(authentication);
            securityContextMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            ResponseEntity<Response> response = momofinAdminController.deleteUser(userId);

            // Assert
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            verify(organizationService).deleteUser(isNull(), eq(UUID.fromString(userId)), eq(requestingUser));
            sentryMock.verify(() ->
                    Sentry.captureMessage(String.format(
                            "[Success] User deleted by Momofin admin - UserID: %s",
                            userId
                    ))
            );
        }
    }

    @Test
    void deleteUser_UserDeletionException_AsMomofinAdmin() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        User requestingUser = new User();
        requestingUser.setMomofinAdmin(true);

        when(userService.fetchUserByUsername(anyString())).thenReturn(requestingUser);
        doThrow(new UserDeletionException("Cannot delete other Momofin admins"))
                .when(organizationService).deleteUser(any(), any(UUID.class), any(User.class));

        try (MockedStatic<SecurityContextHolder> securityContextMock = Mockito.mockStatic(SecurityContextHolder.class)) {
            // Mock security context
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            when(authentication.getName()).thenReturn("admin");
            when(securityContext.getAuthentication()).thenReturn(authentication);
            securityContextMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            ResponseEntity<Response> response = momofinAdminController.deleteUser(userId);

            // Assert
            assertEquals(HttpStatus.GONE, response.getStatusCode());
            assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
            ErrorResponse errorResponse = (ErrorResponse) response.getBody();
            assertEquals("Cannot delete other Momofin admins", errorResponse.getErrorMessage());
        }
    }

    @Test
    void deleteUser_UnexpectedError_AsMomofinAdmin() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        User requestingUser = new User();
        requestingUser.setMomofinAdmin(true);

        when(userService.fetchUserByUsername(anyString())).thenReturn(requestingUser);
        doThrow(new RuntimeException("Unexpected error"))
                .when(organizationService).deleteUser(any(), any(UUID.class), any(User.class));

        try (MockedStatic<SecurityContextHolder> securityContextMock = Mockito.mockStatic(SecurityContextHolder.class)) {
            // Mock security context
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            when(authentication.getName()).thenReturn("admin");
            when(securityContext.getAuthentication()).thenReturn(authentication);
            securityContextMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            ResponseEntity<Response> response = momofinAdminController.deleteUser(userId);

            // Assert
            assertEquals(HttpStatus.GONE, response.getStatusCode());
            assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
            ErrorResponse errorResponse = (ErrorResponse) response.getBody();
            assertTrue(errorResponse.getErrorMessage().contains("Error deleting user"));
        }
    }
}