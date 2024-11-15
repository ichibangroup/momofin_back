package ppl.momofin.momofinbackend.service;


import org.springframework.security.crypto.password.PasswordEncoder;
import ppl.momofin.momofinbackend.error.*;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ppl.momofin.momofinbackend.repository.OrganizationRepository;
import ppl.momofin.momofinbackend.repository.UserRepository;
import ppl.momofin.momofinbackend.utility.PasswordValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    @Autowired
    public UserServiceImpl(UserRepository userRepository, OrganizationRepository organizationRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User authenticate(String organizationName, String username, String password) {
        logger.info("Authenticating user: {} for organization: {}", username, organizationName);

        Organization organization = organizationRepository.findOrganizationByName(organizationName)
                .orElseThrow(() -> new OrganizationNotFoundException(organizationName));

        User user = userRepository.findUserByOrganizationAndUsername(organization, username)
                .orElseThrow(InvalidCredentialsException::new);

        logger.info("User found, checking password");
        boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
        logger.info("Password match result: {}", passwordMatches);

        if (!passwordMatches) {
            logger.warn("Authentication failed for user: {}", username);
            throw new InvalidCredentialsException();
        }

        logger.info("Authentication successful for user: {}", username);
        return user;
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
    public User updateUser(UUID userId, User updatedUser, String oldPassword, String newPassword) {
        User existingUser = getUserById(userId);
        logger.info("Updating user with ID: {}", userId);

        updatePasswordIfRequired(existingUser, oldPassword, newPassword);
        updateUserFields(existingUser, updatedUser);
        User savedUser = userRepository.save(existingUser);
        logger.info("User with ID: {} successfully updated and saved", userId);

        return savedUser;
    }

    private void updatePasswordIfRequired(User user, String oldPassword, String newPassword) {
        if ((oldPassword == null || oldPassword.isEmpty()) && newPassword != null && !newPassword.isEmpty()) {
            throw new InvalidPasswordException("Old password must be provided to change password");
        }
        if (oldPassword != null && !oldPassword.isEmpty()) {
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                throw new InvalidPasswordException("Invalid old password");
            }
            if (newPassword == null || newPassword.isEmpty()) {
                throw new InvalidPasswordException("New password cannot be empty when changing password");
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            logger.info("Password updated for user ID: {}", user.getUserId());
        }
    }

    private void updateUserFields(User existingUser, User updatedUser) {
        if (updatedUser == null) return;

        String newUsername = updatedUser.getUsername();
        if (newUsername != null && !newUsername.isEmpty()
                && !newUsername.equals(existingUser.getUsername())
                && userRepository.findByUsername(newUsername).isPresent()) {
            throw new UserAlreadyExistsException("A user with this username already exists");
        }

        updateField(existingUser, updatedUser.getUsername(), "Username");
        updateField(existingUser, updatedUser.getEmail(), "Email");
        updateField(existingUser, updatedUser.getName(), "Name");
        updateField(existingUser, updatedUser.getPosition(), "Position");
    }

    private void updateField(User existingUser, String newValue, String fieldName) {
        if (newValue != null && !newValue.isEmpty()) {
            switch (fieldName) {
                case "Username":
                    existingUser.setUsername(newValue);
                    break;
                case "Email":
                    existingUser.setEmail(newValue);
                    break;
                case "Name":
                    existingUser.setName(newValue);
                    break;
                case "Position":
                    existingUser.setPosition(newValue);
                    break;
                default:
                    logger.warn("Unrecognized field: {}", fieldName);
                    break;
            }
            logger.info("{} updated for user ID: {}", fieldName, existingUser.getUserId());
        }
    }

    @Override
    public List<User> fetchUsersByOrganization(Organization organization) {
        return userRepository.findAllByOrganization(organization);
    }

    @Override
    public List<User> fetchAllUsers() {
        return userRepository.findAll().stream()
                .filter(user -> !user.getUsername().equals("deleted_user"))
                .toList();
    }
    @Override
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }



    @Override
    public User fetchUserByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.orElse(null);
    }
    @Override
    public User registerOrganizationAdmin(Organization organization, String username, String name, String email, String password, String position) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException("An admin with this username already exists");
        }
        User newUser = registerMember(organization, username, name, email, password, position);
        newUser.setOrganizationAdmin(true);
        return userRepository.save(newUser);
    }
    protected void updateFieldForTesting(User user, String fieldName, String value) {
        updateField(user, value, fieldName);
    }
}
