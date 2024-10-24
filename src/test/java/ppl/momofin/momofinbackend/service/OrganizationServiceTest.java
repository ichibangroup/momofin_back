package ppl.momofin.momofinbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ppl.momofin.momofinbackend.dto.UserDTO;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.repository.OrganizationRepository;
import ppl.momofin.momofinbackend.repository.UserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
        testOrg = new Organization("Test Org", "Test Description");
        testOrg.setOrganizationId(1L);
        testUser = new User(testOrg, "testuser", "Test User", "test@example.com", "password", "Developer", false);
        testUserDTO = UserDTO.fromUser(testUser);
    }

    @Test
    void getUsersInOrganization_ShouldReturnListOfUserDTOs() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrg));
        when(userRepository.findByOrganization(testOrg)).thenReturn(Arrays.asList(testUser));

        List<UserDTO> result = organizationService.getUsersInOrganization(1L);

        assertEquals(1, result.size());
        assertEquals(testUserDTO.getUsername(), result.get(0).getUsername());
    }

    @Test
    void removeUserFromOrganization_ShouldRemoveUserSuccessfully() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrg));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        organizationService.removeUserFromOrganization(1L, 1L);

        verify(userRepository).delete(testUser);
    }

    @Test
    void removeUserFromOrganization_ShouldThrowException_WhenUserNotInOrganization() {
        Organization anotherOrg = new Organization("Another Org", "Another Description");
        anotherOrg.setOrganizationId(2L);
        User userInAnotherOrg = new User(anotherOrg, "anotheruser", "Another User", "another@example.com", "password", "Developer", false);

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrg));
        when(userRepository.findById(2L)).thenReturn(Optional.of(userInAnotherOrg));

        assertThrows(IllegalArgumentException.class, () -> organizationService.removeUserFromOrganization(1L, 2L));
    }

    @Test
    void updateUserInOrganization_ShouldUpdateUserSuccessfully() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrg));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDTO updatedUserDTO = new UserDTO(null, "updateduser", "Updated User", "updated@example.com", "Senior Developer", false);
        UserDTO result = organizationService.updateUserInOrganization(1L, 1L, updatedUserDTO);

        assertEquals(updatedUserDTO.getUsername(), result.getUsername());
        assertEquals(updatedUserDTO.getName(), result.getName());
        assertEquals(updatedUserDTO.getEmail(), result.getEmail());
        assertEquals(updatedUserDTO.getPosition(), result.getPosition());
    }

    @Test
    void updateUserInOrganization_ShouldThrowException_WhenUserNotInOrganization() {
        Organization anotherOrg = new Organization("Another Org", "Another Description");
        anotherOrg.setOrganizationId(2L);
        User userInAnotherOrg = new User(anotherOrg, "anotheruser", "Another User", "another@example.com", "password", "Developer", false);

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrg));
        when(userRepository.findById(2L)).thenReturn(Optional.of(userInAnotherOrg));

        UserDTO updatedUserDTO = new UserDTO(null, "updateduser", "Updated User", "updated@example.com", "Senior Developer", false);
        assertThrows(IllegalArgumentException.class, () -> organizationService.updateUserInOrganization(1L, 2L, updatedUserDTO));
    }

    @Test
    void findOrganizationById_ShouldThrowException_WhenOrganizationNotFound() {
        when(organizationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> organizationService.getUsersInOrganization(999L));
    }

    @Test
    void findUserById_ShouldThrowException_WhenUserNotFound() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrg));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> organizationService.removeUserFromOrganization(1L, 999L));
    }
    @Test
    void updateOrganization_ShouldUpdateAndReturnOrganization() {
        Long orgId = 1L;
        String newName = "Updated Org Name";
        String newDescription = "Updated Org Description";

        Organization existingOrg = new Organization("Old Org Name", "Old Org Description");
        existingOrg.setOrganizationId(orgId);

        Organization updatedOrg = new Organization(newName, newDescription);
        updatedOrg.setOrganizationId(orgId);

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(existingOrg));
        when(organizationRepository.save(any(Organization.class))).thenReturn(updatedOrg);

        Organization result = organizationService.updateOrganization(orgId, newName, newDescription);

        assertNotNull(result);
        assertEquals(orgId, result.getOrganizationId());
        assertEquals(newName, result.getName());
        assertEquals(newDescription, result.getDescription());

        verify(organizationRepository).findById(orgId);
        verify(organizationRepository).save(any(Organization.class));
    }

    @Test
    void updateOrganization_ShouldThrowException_WhenOrganizationNotFound() {
        Long nonExistentOrgId = 999L;
        when(organizationRepository.findById(nonExistentOrgId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                organizationService.updateOrganization(nonExistentOrgId, "New Name", "New Description")
        );

        verify(organizationRepository).findById(nonExistentOrgId);
        verify(organizationRepository, never()).save(any(Organization.class));
    }
}