package ppl.momofin.momofinbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ppl.momofin.momofinbackend.dto.UserDTO;
import ppl.momofin.momofinbackend.error.InvalidOrganizationException;
import ppl.momofin.momofinbackend.error.OrganizationNotFoundException;
import ppl.momofin.momofinbackend.error.SecurityValidationException;
import ppl.momofin.momofinbackend.error.UserDeletionException;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.repository.OrganizationRepository;
import ppl.momofin.momofinbackend.repository.UserRepository;
import ppl.momofin.momofinbackend.security.SqlInjectionValidator;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserRepository userRepository;
    @Mock
    private SqlInjectionValidator sqlInjectionValidator;

    @InjectMocks
    private OrganizationServiceImpl organizationService;

    private Organization testOrg;
    private User testUser;
    private UserDTO testUserDTO;
    private UUID organizationId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testOrg = new Organization("Test Org", "Test Description", "Test Industry", "Test Location");
        organizationId = UUID.fromString("ebe2e5c8-1434-4f91-a5f5-da690db03a6a");
        userId = UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe");
        testOrg.setOrganizationId(organizationId);
        testUser = new User(testOrg, "testuser", "Test User", "test@example.com", "password", "Developer", false);
        testUserDTO = UserDTO.fromUser(testUser);

        when(sqlInjectionValidator.containsSqlInjection(any())).thenReturn(false);
    }

    @Test
    void getUsersInOrganization_ShouldReturnListOfUserDTOs() {
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(testOrg));
        when(userRepository.findByOrganization(testOrg)).thenReturn(Arrays.asList(testUser));

        List<UserDTO> result = organizationService.getUsersInOrganization(organizationId);

        assertEquals(1, result.size());
        assertEquals(testUserDTO.getUsername(), result.get(0).getUsername());
    }

    @Test
    void updateUserInOrganization_ShouldUpdateUserSuccessfully() {
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(testOrg));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDTO updatedUserDTO = new UserDTO(null, "updateduser", "Updated User",
                "updated@example.com", "Senior Developer", false, false);  // Added isMomofinAdmin
        UserDTO result = organizationService.updateUserInOrganization(organizationId, userId, updatedUserDTO);

        assertEquals(updatedUserDTO.getUsername(), result.getUsername());
        assertEquals(updatedUserDTO.getName(), result.getName());
        assertEquals(updatedUserDTO.getEmail(), result.getEmail());
        assertEquals(updatedUserDTO.getPosition(), result.getPosition());
    }

    @Test
    void updateUserInOrganization_ShouldThrowException_WhenUserNotInOrganization() {
        Organization anotherOrg = new Organization("Another Org", "Another Description");
        UUID otherOrgId = UUID.fromString("7fd65283-9521-4bdd-ae71-c8bb83a4b899");
        UUID otherUserId = UUID.fromString("a7d8826a-e47b-4aea-ad9b-8e7c4bfd09a8");
        anotherOrg.setOrganizationId(otherOrgId);
        User userInAnotherOrg = new User(anotherOrg, "anotheruser", "Another User",
                "another@example.com", "password", "Developer", false);

        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(testOrg));
        when(userRepository.findById(otherUserId)).thenReturn(Optional.of(userInAnotherOrg));

        UserDTO updatedUserDTO = new UserDTO(null, "updateduser", "Updated User",
                "updated@example.com", "Senior Developer", false, false);  // Added isMomofinAdmin
        assertThrows(IllegalArgumentException.class,
                () -> organizationService.updateUserInOrganization(organizationId, otherUserId, updatedUserDTO));
    }
    @Test
    void updateOrganization_ShouldUpdateAndReturnOrganization() {
        String newName = "Updated Org Name";
        String newDescription = "Updated Org Description";
        String newIndustry = "Updated Org Industry";
        String newLocation = "Updated Org Location";

        Organization updatedOrg = new Organization(newName, newDescription, newIndustry, newLocation);
        testOrg.setOrganizationId(organizationId);
        updatedOrg.setOrganizationId(organizationId);

        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(testOrg));
        when(organizationRepository.save(any(Organization.class))).thenReturn(updatedOrg);

        Organization result = organizationService.updateOrganization(organizationId, newName, newDescription, newIndustry, newLocation);

        assertNotNull(result);
        assertEquals(organizationId, result.getOrganizationId());
        assertEquals(newName, result.getName());
        assertEquals(newDescription, result.getDescription());
        assertEquals(newIndustry, result.getIndustry());
        assertEquals(newLocation, result.getLocation());

        verify(organizationRepository).findById(organizationId);
        verify(organizationRepository).save(any(Organization.class));
    }

    @Test
    void updateOrganization_ShouldThrowException_WhenOrganizationNotFound() {
        UUID nonExistentOrgId = UUID.fromString("7fd65283-9521-4bdd-ae71-c8bb83a4b899");
        when(organizationRepository.findById(nonExistentOrgId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                organizationService.updateOrganization(nonExistentOrgId, "New Name", "New Description", "New Industry", "New Location")
        );

        verify(organizationRepository).findById(nonExistentOrgId);
        verify(organizationRepository, never()).save(any(Organization.class));
    }

    @Test
    void getAllOrganizations_shouldReturnAllOrganizations() {
        // Arrange
        List<Organization> organizations = Arrays.asList(
                new Organization("Org1", "Desc1"),
                new Organization("Org2", "Desc2")
        );
        when(organizationRepository.findAll()).thenReturn(organizations);

        // Act
        List<Organization> result = organizationService.getAllOrganizations();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Org1", result.get(0).getName());
        assertEquals("Org2", result.get(1).getName());
    }

    @Test
    void createOrganization_shouldCreateAndReturnNewOrganization() {
        // Arrange
        Organization newOrg = new Organization("New Org", "New Desc", "New Industry", "New Location");
        when(organizationRepository.save(any(Organization.class))).thenReturn(newOrg);

        // Act
        Organization result = organizationService.createOrganization("New Org", "New Desc", "New Industry", "New Location");

        // Assert
        assertEquals("New Org", result.getName());
        assertEquals("New Desc", result.getDescription());
    }

    @Test
    void constructor_shouldInitializeCorrectly() {
        // Arrange
        MockitoAnnotations.openMocks(this);
        when(organizationRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        OrganizationService newOrganizationService = new OrganizationServiceImpl(
                organizationRepository,
                userRepository,
                sqlInjectionValidator
        );

        // Assert
        assertNotNull(newOrganizationService);
        assertDoesNotThrow(newOrganizationService::getAllOrganizations);
        verify(organizationRepository).findAll();
    }

    @Test
    void updateOrganization_shouldThrowException_whenOrgIdIsNull() {
        assertThrows(InvalidOrganizationException.class,
                () -> organizationService.updateOrganization(null, "Name", "Description", "Industry", "Location"));
    }

    @Test
    void updateOrganization_shouldThrowException_whenNameIsEmpty() {
        assertThrows(InvalidOrganizationException.class,
                () -> organizationService.updateOrganization(organizationId, "", "Description", "Industry", "Location"));
    }

    @Test
    void updateOrganization_shouldThrowException_whenNameIsNull() {
        assertThrows(InvalidOrganizationException.class,
                () -> organizationService.updateOrganization(organizationId, null, "Description", "Industry", "Location"));
    }

    @Test
    void updateOrganization_shouldThrowException_whenOrganizationNotFound() {
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.empty());
        assertThrows(OrganizationNotFoundException.class,
                () -> organizationService.updateOrganization(organizationId, "Name", "Description", "Industry", "Location"));
    }

    @Test
    void createOrganization_shouldThrowException_whenNameIsEmpty() {
        assertThrows(InvalidOrganizationException.class,
                () -> organizationService.createOrganization("", "Description", "Industry", "Location"));
    }

    @Test
    void createOrganization_shouldThrowException_whenNameIsNull() {
        assertThrows(InvalidOrganizationException.class,
                () -> organizationService.createOrganization(null, "Description", "Industry", "Location"));
    }

    @Test
    void findOrganization_success() {
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(testOrg));
        Organization organization = organizationService.findOrganizationById(organizationId);
        assertEquals(testOrg, organization);
    }
    @Test
    void findOrganization_shouldThrowException_whenOrganizationNotFound() {
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.empty());
        assertThrows(OrganizationNotFoundException.class,
                () -> organizationService.findOrganizationById(organizationId));
    }
    @Test
    void deleteUser_Success() {
        // Setup
        Organization org = new Organization("Test Org", "Test Description");
        org.setOrganizationId(organizationId);

        User adminUser = new User();
        adminUser.setOrganization(org);
        adminUser.setOrganizationAdmin(true);
        adminUser.setUsername("admin");

        User userToDelete = new User();
        userToDelete.setUserId(userId);
        userToDelete.setOrganization(org);
        userToDelete.setOrganizationAdmin(false);
        userToDelete.setUsername("user");

        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(org));
        when(userRepository.findById(userId)).thenReturn(Optional.of(userToDelete));

        // Execute
        organizationService.deleteUser(organizationId, userId, adminUser);

        // Verify
        verify(userRepository).delete(userToDelete);
    }

    @Test
    void deleteUser_ThrowsException_WhenNotOrganizationAdmin() {
        // Setup
        Organization org = new Organization("Test Org", "Test Description");
        org.setOrganizationId(organizationId);
        User regularUser = new User();
        regularUser.setOrganization(org);
        regularUser.setOrganizationAdmin(false);

        User userToDelete = new User();
        userToDelete.setUserId(userId);

        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(org));
        when(userRepository.findById(userId)).thenReturn(Optional.of(userToDelete));

        // Execute & Verify
        assertThrows(UserDeletionException.class, () ->
                organizationService.deleteUser(organizationId, userId, regularUser)
        );
    }

    @Test
    void deleteUser_ThrowsException_WhenDifferentOrganization() {
        // Setup
        Organization org1 = new Organization("Org 1", "Desc 1");
        org1.setOrganizationId(organizationId);
        UUID otherOrgId = UUID.fromString("7fd65283-9521-4bdd-ae71-c8bb83a4b899");
        Organization org2 = new Organization("Org 2", "Desc 2");
        org2.setOrganizationId(otherOrgId);

        User adminUser = new User();
        adminUser.setOrganization(org1);
        adminUser.setOrganizationAdmin(true);

        User userToDelete = new User();
        userToDelete.setUserId(userId);
        userToDelete.setOrganization(org2);

        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(org1));
        when(userRepository.findById(userId)).thenReturn(Optional.of(userToDelete));

        // Execute & Verify
        assertThrows(UserDeletionException.class, () ->
                organizationService.deleteUser(organizationId, userId, adminUser)
        );
    }

    @Test
    void deleteUser_ThrowsException_WhenDeletingOrgAdmin() {
        // Setup
        Organization org = new Organization("Test Org", "Test Description");
        org.setOrganizationId(organizationId);

        User adminUser = new User();
        adminUser.setOrganization(org);
        adminUser.setOrganizationAdmin(true);

        User anotherAdmin = new User();
        anotherAdmin.setUserId(userId);
        anotherAdmin.setOrganization(org);
        anotherAdmin.setOrganizationAdmin(true);

        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(org));
        when(userRepository.findById(userId)).thenReturn(Optional.of(anotherAdmin));

        // Execute & Verify
        assertThrows(UserDeletionException.class, () ->
                organizationService.deleteUser(organizationId, userId, adminUser)
        );
    }

    @Test
    void deleteUser_ThrowsException_WhenOrganizationNotFound() {
        // Setup
        User adminUser = new User();
        adminUser.setOrganizationAdmin(true);

        when(organizationRepository.findById(organizationId)).thenReturn(Optional.empty());

        // Execute & Verify
        assertThrows(OrganizationNotFoundException.class, () ->
                organizationService.deleteUser(organizationId, userId, adminUser)
        );
    }
    @Test
    void deleteUser_ThrowsException_WhenAdminFromDifferentOrganization() {
        // Setup
        Organization org1 = new Organization("Org 1", "Desc 1");
        org1.setOrganizationId(organizationId);
        UUID otherOrgId = UUID.fromString("7fd65283-9521-4bdd-ae71-c8bb83a4b899");
        Organization adminOrg = new Organization("Admin Org", "Admin Desc");
        adminOrg.setOrganizationId(otherOrgId);

        User adminUser = new User();
        adminUser.setOrganization(adminOrg);  // Admin is from a different org
        adminUser.setOrganizationAdmin(true);

        User userToDelete = new User();
        userToDelete.setUserId(userId);
        userToDelete.setOrganization(org1);

        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(org1));
        when(userRepository.findById(userId)).thenReturn(Optional.of(userToDelete));

        // Execute & Verify
        assertThrows(UserDeletionException.class, () ->
                organizationService.deleteUser(organizationId, userId, adminUser)
        );
    }
    @Test
    void validateInputs_WithSpecialCharacters() {
        // Arrange
        String nameWithSpecialChars = "John's Hardware & Tools";
        Organization expectedOrg = new Organization(nameWithSpecialChars, "Description", "Retail", "NY");

        when(sqlInjectionValidator.containsSqlInjection(any())).thenReturn(false);
        when(organizationRepository.save(any(Organization.class))).thenReturn(expectedOrg);

        // Act
        Organization result = organizationService.createOrganization(
                nameWithSpecialChars, "Description", "Retail", "NY");

        // Assert
        assertEquals(nameWithSpecialChars, result.getName());
    }

    @Test
    void createOrganization_WithMultipleValidations() {
        // Test that all fields are validated
        when(sqlInjectionValidator.containsSqlInjection("name")).thenReturn(false);
        when(sqlInjectionValidator.containsSqlInjection("description")).thenReturn(false);
        when(sqlInjectionValidator.containsSqlInjection("MALICIOUS")).thenReturn(true);

        assertThrows(SecurityValidationException.class, () ->
                organizationService.createOrganization("name", "description", "MALICIOUS", "location")
        );
    }
    @Test
    void deleteUser_ThrowsException_WhenUserAlreadyDeleted() {
        // Setup
        Organization org = new Organization("Test Org", "Test Description");
        UUID orgId = UUID.randomUUID();
        org.setOrganizationId(orgId);

        User adminUser = new User();
        adminUser.setOrganization(org);
        adminUser.setOrganizationAdmin(true);

        UUID userId = UUID.randomUUID();

        // Mock user not found (already deleted) scenario
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Execute & Verify
        UserDeletionException exception = assertThrows(UserDeletionException.class, () ->
                organizationService.deleteUser(orgId, userId, adminUser)
        );
        assertEquals("User no longer exists or was already deleted", exception.getMessage());
    }
    @Test
    void deleteUser_ThrowsException_WhenUserNotFound() {
        // Setup
        UUID orgId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Organization org = new Organization("Test Org", "Test Description");
        org.setOrganizationId(orgId);

        User adminUser = new User();
        adminUser.setOrganization(org);
        adminUser.setOrganizationAdmin(true);

        // Mock organization found but user not found
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Execute & Verify
        UserDeletionException exception = assertThrows(UserDeletionException.class, () ->
                organizationService.deleteUser(orgId, userId, adminUser)
        );
        assertEquals("User no longer exists or was already deleted", exception.getMessage());
    }


}