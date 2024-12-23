package ppl.momofin.momofinbackend.service;

import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.model.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    User authenticate(String organizationName, String username, String password);
    User save(User user);
    User registerMember(Organization organization, String username, String name, String email, String password, String position);
    List<User> fetchUsersByOrganization(Organization organization);
    List<User> fetchAllUsers();
    User getUserById(UUID userId);
    User updateUser(UUID userId, User updatedUser, String oldPassword, String newPassword);
    User fetchUserByUsername(String username);
    User registerOrganizationAdmin(Organization organization, String username, String name, String email, String password, String position);
}
