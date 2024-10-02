package ppl.momofin.momofinbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.repository.OrganizationRepository;
import ppl.momofin.momofinbackend.repository.UserRepository;

import java.util.List;

@Service
public class OrganizationService {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    public Organization updateOrganization(Long orgId, String name, String description) {
        Organization org = findOrganizationById(orgId);
        org.setName(name);
        org.setDescription(description);
        return organizationRepository.save(org);
    }

    public List<User> getUsersInOrganization(Long orgId) {
        Organization org = findOrganizationById(orgId);
        return userRepository.findByOrganization(org);
    }

    public User addUserToOrganization(Long orgId, User user) {
        Organization org = findOrganizationById(orgId);
        user.setOrganization(org);
        return userRepository.save(user);
    }

    public void removeUserFromOrganization(Long orgId, Long userId) {
        Organization org = findOrganizationById(orgId);
        User user = findUserById(userId);
        if (!user.getOrganization().equals(org)) {
            throw new IllegalArgumentException("User does not belong to this organization");
        }
        userRepository.delete(user);
    }

    public User updateUserInOrganization(Long orgId, Long userId, User updatedUser) {
        Organization org = findOrganizationById(orgId);
        User user = findUserById(userId);
        if (!user.getOrganization().equals(org)) {
            throw new IllegalArgumentException("User does not belong to this organization");
        }
        user.setName(updatedUser.getName());
        user.setEmail(updatedUser.getEmail());
        user.setPassword(updatedUser.getPassword());
        user.setPosition(updatedUser.getPosition());
        user.setUsername(updatedUser.getUsername());
        return userRepository.save(user);
    }

    private Organization findOrganizationById(Long orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}