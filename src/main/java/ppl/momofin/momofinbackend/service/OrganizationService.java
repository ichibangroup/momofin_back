package ppl.momofin.momofinbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ppl.momofin.momofinbackend.dto.UserDTO;
import ppl.momofin.momofinbackend.error.InvalidOrganizationException;
import ppl.momofin.momofinbackend.error.OrganizationNotFoundException;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.repository.OrganizationRepository;
import ppl.momofin.momofinbackend.repository.UserRepository;

import java.util.List;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    private final UserRepository userRepository;

    @Autowired
    public OrganizationService(OrganizationRepository organizationRepository, UserRepository userRepository) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
    }

    public Organization updateOrganization(Long orgId, String name, String description, String industry, String location) {
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

    public List<UserDTO> getUsersInOrganization(Long orgId) {
        Organization org = findOrganizationById(orgId);
        return userRepository.findByOrganization(org).stream()
                .map(UserDTO::fromUser)
                .toList();
    }

    public void removeUserFromOrganization(Long orgId, Long userId) {
        Organization org = findOrganizationById(orgId);
        User user = findUserById(userId);
        if (!user.getOrganization().equals(org)) {
            throw new IllegalArgumentException("User does not belong to this organization");
        }
        userRepository.delete(user);
    }

    public UserDTO updateUserInOrganization(Long orgId, Long userId, UserDTO updatedUserDTO) {
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

    public Organization findOrganizationById(Long orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(OrganizationNotFoundException::new);
    }

    private User findUserById(Long userId) {
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
}