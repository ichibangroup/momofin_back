package ppl.momofin.momofinbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ppl.momofin.momofinbackend.dto.UserDTO;
import ppl.momofin.momofinbackend.error.InvalidOrganizationException;
import ppl.momofin.momofinbackend.error.OrganizationNotFoundException;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.repository.OrganizationRepository;
import ppl.momofin.momofinbackend.repository.UserRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrganizationService organizationService;

    private Organization testOrg;
    private User testUser;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testOrg = new Organization("Test Org", "Test Description", "Test Industry", "Test Location");
        UUID organizationId = UUID.fromString("ebe4972e-892b-4b3d-a107-ac739aaa6434");
        testOrg.setOrganizationId(organizationId);
        testUser = new User(testOrg, "testuser", "Test User", "test@example.com", "password", "Developer", false);
        testUserDTO = UserDTO.fromUser(testUser);
    }

    @Test
    void getUsersInOrganization_ShouldReturnListOfUserDTOs() {
        UUID organizationId = UUID.fromString("ebe4972e-892b-4b3d-a107-ac739aaa6434");
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(testOrg));
        when(userRepository.findByOrganization(testOrg)).thenReturn(Arrays.asList(testUser));

        List<UserDTO> result = organizationService.getUsersInOrganization(organizationId);

        assertEquals(1, result.size());
        assertEquals(testUserDTO.getUsername(), result.get(0).getUsername());
    }

    @Test
    void removeUserFromOrganization_ShouldRemoveUserSuccessfully() {
        UUID userId = UUID.fromString("ff354956-c4c4-4697-9814-e34cd5ef5d4b");
        UUID organizationId = UUID.fromString("ebe4972e-892b-4b3d-a107-ac739aaa6434");
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(testOrg));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        organizationService.removeUserFromOrganization(organizationId, userId);

        verify(userRepository).delete(testUser);
    }

    @Test
    void removeUserFromOrganization_ShouldThrowException_WhenUserNotInOrganization() {
        Organization anotherOrg = new Organization("Another Org", "Another Description");
        UUID organizationId = UUID.fromString("ebe4972e-892b-4b3d-a107-ac739aaa6434");
        anotherOrg.setOrganizationId(organizationId);
        User userInAnotherOrg = new User(anotherOrg, "anotheruser", "Another User", "another@example.com", "password", "Developer", false);

        UUID userId = UUID.fromString("ff354956-c4c4-4697-9814-e34cd5ef5d4b");
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(testOrg));
        when(userRepository.findById(userId)).thenReturn(Optional.of(userInAnotherOrg));

        assertThrows(IllegalArgumentException.class, () -> organizationService.removeUserFromOrganization(organizationId, userId));
    }

    @Test
    void updateUserInOrganization_ShouldUpdateUserSuccessfully() {
        UUID userId = UUID.fromString("ff354956-c4c4-4697-9814-e34cd5ef5d4b");
        UUID organizationId = UUID.fromString("ebe4972e-892b-4b3d-a107-ac739aaa6434");
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(testOrg));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDTO updatedUserDTO = new UserDTO(null, "updateduser", "Updated User", "updated@example.com", "Senior Developer", false);
        UserDTO result = organizationService.updateUserInOrganization(organizationId, userId, updatedUserDTO);

        assertEquals(updatedUserDTO.getUsername(), result.getUsername());
        assertEquals(updatedUserDTO.getName(), result.getName());
        assertEquals(updatedUserDTO.getEmail(), result.getEmail());
        assertEquals(updatedUserDTO.getPosition(), result.getPosition());
    }

    @Test
    void updateUserInOrganization_ShouldThrowException_WhenUserNotInOrganization() {
        UUID userId = UUID.fromString("ff354956-c4c4-4697-9814-e34cd5ef5d4b");
        Organization anotherOrg = new Organization("Another Org", "Another Description");
        UUID organizationId = UUID.fromString("ebe4972e-892b-4b3d-a107-ac739aaa6434");
        anotherOrg.setOrganizationId(organizationId);
        User userInAnotherOrg = new User(anotherOrg, "anotheruser", "Another User", "another@example.com", "password", "Developer", false);

        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(testOrg));
        when(userRepository.findById(userId)).thenReturn(Optional.of(userInAnotherOrg));

        UserDTO updatedUserDTO = new UserDTO(null, "updateduser", "Updated User", "updated@example.com", "Senior Developer", false);
        assertThrows(IllegalArgumentException.class, () -> organizationService.updateUserInOrganization(organizationId, userId, updatedUserDTO));
    }

    @Test
    void findOrganizationById_ShouldThrowException_WhenOrganizationNotFound() {
        UUID organizationId = UUID.fromString("ebe4972e-892b-4b3d-a107-ac739aaa6434");

        when(organizationRepository.findById(organizationId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> organizationService.getUsersInOrganization(organizationId));
    }

    @Test
    void findUserById_ShouldThrowException_WhenUserNotFound() {
        UUID userId = UUID.fromString("ff354956-c4c4-4697-9814-e34cd5ef5d4b");
        UUID organizationId = UUID.fromString("ebe4972e-892b-4b3d-a107-ac739aaa6434");

        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(testOrg));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> organizationService.removeUserFromOrganization(organizationId, userId));
    }
    @Test
    void updateOrganization_ShouldUpdateAndReturnOrganization() {
        UUID orgId = UUID.fromString("ff354956-c4c4-4697-9814-e34cd5ef5d4b");
        String newName = "Updated Org Name";
        String newDescription = "Updated Org Description";
        String newIndustry = "Updated Org Industry";
        String newLocation = "Updated Org Location";

        Organization updatedOrg = new Organization(newName, newDescription, newIndustry, newLocation);
        testOrg.setOrganizationId(orgId);
        updatedOrg.setOrganizationId(orgId);

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(testOrg));
        when(organizationRepository.save(any(Organization.class))).thenReturn(updatedOrg);

        Organization result = organizationService.updateOrganization(orgId, newName, newDescription, newIndustry, newLocation);

        assertNotNull(result);
        assertEquals(orgId, result.getOrganizationId());
        assertEquals(newName, result.getName());
        assertEquals(newDescription, result.getDescription());
        assertEquals(newIndustry, result.getIndustry());
        assertEquals(newLocation, result.getLocation());

        verify(organizationRepository).findById(orgId);
        verify(organizationRepository).save(any(Organization.class));
    }

    @Test
    void updateOrganization_ShouldThrowException_WhenOrganizationNotFound() {
        UUID nonExistentOrgId = UUID.fromString("ebe4972e-892b-4b3d-a107-ac739aaa6434");

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
        OrganizationService newOrganizationService = new OrganizationService(organizationRepository, userRepository);

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
        UUID organizationId = UUID.fromString("ebe4972e-892b-4b3d-a107-ac739aaa6434");

        assertThrows(InvalidOrganizationException.class,
                () -> organizationService.updateOrganization(organizationId, "", "Description", "Industry", "Location"));
    }

    @Test
    void updateOrganization_shouldThrowException_whenNameIsNull() {
        UUID organizationId = UUID.fromString("ebe4972e-892b-4b3d-a107-ac739aaa6434");

        assertThrows(InvalidOrganizationException.class,
                () -> organizationService.updateOrganization(organizationId, null, "Description", "Industry", "Location"));
    }

    @Test
    void updateOrganization_shouldThrowException_whenOrganizationNotFound() {
        UUID organizationId = UUID.fromString("ebe4972e-892b-4b3d-a107-ac739aaa6434");

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
        UUID organizationId = UUID.fromString("ebe4972e-892b-4b3d-a107-ac739aaa6434");

        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(testOrg));
        Organization organization = organizationService.findOrganizationById(organizationId);
        assertEquals(testOrg, organization);
    }
    @Test
    void findOrganization_shouldThrowException_whenOrganizationNotFound() {
        UUID organizationId = UUID.fromString("ebe4972e-892b-4b3d-a107-ac739aaa6434");

        when(organizationRepository.findById(organizationId)).thenReturn(Optional.empty());
        assertThrows(OrganizationNotFoundException.class,
                () -> organizationService.findOrganizationById(organizationId));
    }
}