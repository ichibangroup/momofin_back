package service;


import model.Organization;
import model.User;

import java.util.List;

public class UserServiceImpl implements UserService{
    @Override
    public User authenticate(String organizationName, String email, String password) {
        return null;
    }

    @Override
    public User save(User user) {
        return null;
    }

    @Override
    public User registerMember(Organization organization, String name, String email, String password, String position) {
        return null;
    }

    @Override
    public List<User> fetchUsersByOrganization(Organization organization) {
        return null;
    }

    @Override
    public List<User> fetchAllUsers() {
        return null;
    }
}
