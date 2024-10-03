package ppl.momofin.momofinbackend.service;

import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.model.User;

import java.util.List;

public interface UserService {
    User authenticate(String organizationName, String username, String password);
    User save(User user);
    User registerMember(Organization organization, String username, String name, String email, String password, String position);
    List<User> fetchUsersByOrganization(Organization organization);
    List<User> fetchAllUsers();
    User getUserById(Long userId);
    User updateUser(Long userId, User updatedUser);
    User fetchUserByUsername(String username);
}
