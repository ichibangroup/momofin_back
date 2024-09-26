package ppl.momofin.momofinbackend.service;


import org.springframework.security.crypto.password.PasswordEncoder;
import ppl.momofin.momofinbackend.error.InvalidCredentialsException;
import ppl.momofin.momofinbackend.error.OrganizationNotFoundException;
import ppl.momofin.momofinbackend.error.UserAlreadyExistsException;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ppl.momofin.momofinbackend.repository.OrganizationRepository;
import ppl.momofin.momofinbackend.repository.UserRepository;
import ppl.momofin.momofinbackend.utility.PasswordValidator;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService{
    @Autowired
    UserRepository userRepository;
    @Autowired
    OrganizationRepository organizationRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public User authenticate(String organizationName, String username, String password) {
        Optional<Organization> organization = organizationRepository.findOrganizationByName(organizationName);

        if(organization.isEmpty()) {
            throw new OrganizationNotFoundException(organizationName);
        }

        Optional<User> user = userRepository.findUserByOrganizationAndUsername(organization.get(), username);

        if(user.isEmpty()|| !passwordEncoder.matches(password, user.get().getPassword())) {
            throw new InvalidCredentialsException();
        }

        return user.get();
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User registerMember(Organization organization, String username, String name, String email, String password, String position) {
        PasswordValidator.validatePassword(password);

        List<User> fetchResults = userRepository.findUserByUsernameOrEmail(username, email);

        if (!fetchResults.isEmpty()) {
            User existingUser = fetchResults.getFirst();

            if (email.equals(existingUser.getEmail())) {
                throw new UserAlreadyExistsException("The email "+email+" is already in use");
            } else if (username.equals(existingUser.getUsername())) {
                throw new UserAlreadyExistsException("The username "+username+" is already in use");
            }
        }

        String encodedPassword = passwordEncoder.encode(password);

        return userRepository.save(new User(organization, username, name, email, encodedPassword, position));
    }

    @Override
    public List<User> fetchUsersByOrganization(Organization organization) {
        return userRepository.findAllByOrganization(organization);
    }

    @Override
    public List<User> fetchAllUsers() {
        return userRepository.findAll();
    }
}
