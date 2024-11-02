package ppl.momofin.momofinbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ppl.momofin.momofinbackend.dto.UserDTO;
import ppl.momofin.momofinbackend.error.InvalidOrganizationException;
import ppl.momofin.momofinbackend.error.OrganizationNotFoundException;
import ppl.momofin.momofinbackend.error.UserDeletionException;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.repository.OrganizationRepository;
import ppl.momofin.momofinbackend.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    private final UserRepository userRepository;

    @Autowired
    public OrganizationService(OrganizationRepository organizationRepository, UserRepository userRepository) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
    }

    public Organization updateOrganization(UUID orgId, String name, String description, String industry, String location) {
        if (orgId == null) {
            throw new InvalidOrganizationException("Organization ID cannot be null");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidOrganizationException("Organization name cannot be empty");
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found with id: " + orgId));

        org.setName(name);
        org.setDescription(description);
        org.setIndustry(industry);
        org.setLocation(location);
        return organizationRepository.save(org);
    }

    public List<UserDTO> getUsersInOrganization(UUID orgId) {
        Organization org = findOrganizationById(orgId);
        return userRepository.findByOrganization(org).stream()
                .map(UserDTO::fromUser)
                .toList();
    }

    public UserDTO updateUserInOrganization(UUID orgId, UUID userId, UserDTO updatedUserDTO) {
        Organization org = findOrganizationById(orgId);
        User user = findUserById(userId);
        if (!user.getOrganization().equals(org)) {
            throw new IllegalArgumentException("User does not belong to this organization");
        }
        user.setName(updatedUserDTO.getName());
        user.setEmail(updatedUserDTO.getEmail());
        user.setPosition(updatedUserDTO.getPosition());
        user.setUsername(updatedUserDTO.getUsername());
        user.setOrganizationAdmin(updatedUserDTO.isOrganizationAdmin());
        User savedUser = userRepository.save(user);
        return UserDTO.fromUser(savedUser);
    }

    public Organization findOrganizationById(UUID orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(OrganizationNotFoundException::new);
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    @Transactional(readOnly = true)
    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }
    public Organization createOrganization(String name, String description, String industry, String location) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidOrganizationException("Organization name cannot be empty");
        }
        Organization newOrganization = new Organization(name, description, industry, location);
        return organizationRepository.save(newOrganization);
    }
    @Transactional
    public void deleteUser(UUID orgId, UUID userId, User requestingUser) {
        Organization org = findOrganizationById(orgId);
        User userToDelete = findUserById(userId);
        // Don't allow deletion of the system deleted user

        // Permission checks
        if (!requestingUser.isOrganizationAdmin()) {
            throw new UserDeletionException("Only organization admins can delete users");
        }

        if (!requestingUser.getOrganization().equals(org)) {
            throw new UserDeletionException("You can only delete users from your own organization");
        }

        if (!userToDelete.getOrganization().equals(org)) {
            throw new UserDeletionException("User does not belong to your organization");
        }

        if (userToDelete.isOrganizationAdmin()) {
            throw new UserDeletionException("Organization admins cannot be deleted");
        }

        // Delete the user - triggers will handle reference updates
        userRepository.delete(userToDelete);
    }
}