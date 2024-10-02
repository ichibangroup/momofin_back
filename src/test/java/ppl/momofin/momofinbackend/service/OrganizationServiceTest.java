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
    void updateUserInOrganization_ShouldUpdateUserSuccessfully() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrg));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDTO updatedUserDTO = new UserDTO(1L, "updateduser", "Updated User", "updated@example.com", "Senior Developer", false);
        UserDTO result = organizationService.updateUserInOrganization(1L, 1L, updatedUserDTO);

        assertEquals(updatedUserDTO.getUsername(), result.getUsername());
        assertEquals(updatedUserDTO.getName(), result.getName());
        assertEquals(updatedUserDTO.getEmail(), result.getEmail());
        assertEquals(updatedUserDTO.getPosition(), result.getPosition());
    }
}