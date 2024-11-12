package ppl.momofin.momofinbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.UUID;

@Service
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final SqlInjectionValidator sqlInjectionValidator;
    private static final Logger logger = LoggerFactory.getLogger(OrganizationServiceImpl.class);

    @Autowired
    public OrganizationServiceImpl(OrganizationRepository organizationRepository, UserRepository userRepository, SqlInjectionValidator sqlInjectionValidator) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.sqlInjectionValidator = sqlInjectionValidator;
    }

    @Override
    public Organization updateOrganization(UUID orgId, String name, String description, String industry, String location) {
        validateInputs(name, description, industry, location);
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

    @Override
    public List<UserDTO> getUsersInOrganization(UUID orgId) {
        Organization org = findOrganizationById(orgId);
        return userRepository.findByOrganization(org).stream()
                .map(UserDTO::fromUser)
                .toList();
    }

    @Override
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

    @Override
    public Organization findOrganizationById(UUID orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(OrganizationNotFoundException::new);
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserDeletionException("User no longer exists or was already deleted"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    @Override
    public Organization createOrganization(String name, String description, String industry, String location) {
        validateInputs(name, description, industry, location);
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidOrganizationException("Organization name cannot be empty");
        }
        Organization newOrganization = new Organization(name, description, industry, location);
        return organizationRepository.save(newOrganization);
    }

    @Override
    @Transactional
    public void deleteUser(UUID orgId, UUID userId, User requestingUser) {
        User userToDelete = findUserById(userId);

        if (requestingUser.isMomofinAdmin()) {
            // Momofin admin specific checks
            if (userToDelete.isMomofinAdmin()) {
                throw new UserDeletionException("Cannot delete other Momofin admins");
            }
            // Momofin admin can delete any org admin or regular user
            userRepository.delete(userToDelete);
        } else {
            Organization org = findOrganizationById(orgId);
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

            userRepository.delete(userToDelete);
        }



    }
    private void validateInputs(String... inputs) {
        for (String input : inputs) {
            if (input != null && sqlInjectionValidator.containsSqlInjection(input)) {
                logger.error("SQL injection attempt detected in input");
                throw new SecurityValidationException("SQL injection detected in input");
            }
        }
    }
    @Override
    @Transactional
    public void deleteOrganization(UUID orgId) {
        Organization org = findOrganizationById(orgId);

        List<User> orgUsers = userRepository.findByOrganization(org);
        for (User user : orgUsers) {
            if (!user.getUsername().equals("deleted_user") && !user.isMomofinAdmin()) {
                userRepository.delete(user);
            } else if (user.isMomofinAdmin()) {
                user.setOrganizationAdmin(false);
                user.setOrganization(null);
                userRepository.save(user);
            }
        }
        organizationRepository.delete(org);
    }
    @Override
    @Transactional
    public User setOrganizationAdmin(UUID orgId, UUID userId) {
        Organization org = findOrganizationById(orgId);

}