package ppl.momofin.momofinbackend.service;


import org.springframework.security.crypto.password.PasswordEncoder;
import ppl.momofin.momofinbackend.error.InvalidCredentialsException;
import ppl.momofin.momofinbackend.error.OrganizationNotFoundException;
import ppl.momofin.momofinbackend.error.UserAlreadyExistsException;
import ppl.momofin.momofinbackend.error.UserNotFoundException;
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
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, OrganizationRepository organizationRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
    }

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

        Optional<User> fetchResults = userRepository.findByUsername(username);

        if (fetchResults.isPresent()) {
            throw new UserAlreadyExistsException("The username "+username+" is already in use");
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
    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }

    @Override
    public User updateUser(Long userId, User updatedUser) {
        User existingUser = getUserById(userId);

        if (updatedUser.getEmail() != null) {
            existingUser.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getUsername() != null) {
            existingUser.setUsername(updatedUser.getUsername());
        }
        if (updatedUser.getPassword() != null) {
            PasswordValidator.validatePassword(updatedUser.getPassword());
            String encodedPassword = passwordEncoder.encode(updatedUser.getPassword());
            existingUser.setPassword(encodedPassword);
        }

        return userRepository.save(existingUser);
    }

    @Override
    public User fetchUserByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.orElse(null);
    }
}
