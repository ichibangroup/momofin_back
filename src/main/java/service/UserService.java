package service;

import model.Organization;
import model.User;

import java.util.List;

public interface UserService {
    User authenticate(String organizationName, String email, String password);
    User save(User user);
    User registerMember(Organization organization, String name, String email, String password, String position);
    List<User> fetchUsersByOrganization(Organization organization);
    List<User> fetchAllUsers();
}
