package ppl.momofin.momofinbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ppl.momofin.momofinbackend.dto.UserDTO;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.repository.OrganizationRepository;
import ppl.momofin.momofinbackend.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

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

    public List<UserDTO> getUsersInOrganization(Long orgId) {
        Organization org = findOrganizationById(orgId);
        return userRepository.findByOrganization(org).stream()
                .map(UserDTO::fromUser)
                .collect(Collectors.toList());
    }

    public UserDTO addUserToOrganization(Long orgId, UserDTO userDTO) {
        Organization org = findOrganizationById(orgId);
        User user = userDTO.toUser();
        user.setOrganization(org);
        User savedUser = userRepository.save(user);
        return UserDTO.fromUser(savedUser);
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

    private Organization findOrganizationById(Long orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}