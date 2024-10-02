package ppl.momofin.momofinbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testOrg = new Organization("Test Org", "Test Description");
        testOrg.setOrganizationId(1L);
        testUser = new User(testOrg, "testuser", "Test User", "test@example.com", "password", "Developer", false);
    }

    @Test
    void getUsersInOrganization_ShouldReturnListOfUsers() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrg));
        when(userRepository.findByOrganization(testOrg)).thenReturn(Arrays.asList(testUser));

        List<User> result = organizationService.getUsersInOrganization(1L);

        assertEquals(1, result.size());
        assertEquals(testUser, result.get(0));
    }

    @Test
    void addUserToOrganization_ShouldAddUserSuccessfully() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrg));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = organizationService.addUserToOrganization(1L, testUser);

        assertEquals(testUser, result);
        assertEquals(testOrg, result.getOrganization());
    }

    @Test
    void removeUserFromOrganization_ShouldRemoveUserSuccessfully() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrg));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        organizationService.removeUserFromOrganization(1L, 1L);

        verify(userRepository).delete(testUser);
    }

    @Test
    void updateUserInOrganization_ShouldUpdateUserSuccessfully() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrg));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User updatedUser = new User(testOrg, "updateduser", "Updated User", "updated@example.com", "newpassword", "Senior Developer", false);
        User result = organizationService.updateUserInOrganization(1L, 1L, updatedUser);

        assertEquals(updatedUser.getUsername(), result.getUsername());
        assertEquals(updatedUser.getName(), result.getName());
        assertEquals(updatedUser.getEmail(), result.getEmail());
        assertEquals(updatedUser.getPosition(), result.getPosition());
    }
}