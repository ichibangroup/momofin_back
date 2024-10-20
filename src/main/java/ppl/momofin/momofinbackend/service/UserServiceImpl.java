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
                .orElseThrow(() -> new InvalidCredentialsException());

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
    public User updateUser(Long userId, User updatedUser, String oldPassword, String newPassword) {
        User existingUser = getUserById(userId);
        logger.info("Updating user with ID: {}", userId);
        logger.info("Old password provided: {}, New password provided: {}",
                oldPassword != null && !oldPassword.isEmpty(),
                newPassword != null && !newPassword.isEmpty());

        // Password update logic
        if (oldPassword != null && !oldPassword.isEmpty()) {
            if (!passwordEncoder.matches(oldPassword, existingUser.getPassword())) {
                logger.warn("Invalid old password provided for user ID: {}", userId);
                throw new InvalidPasswordException("Invalid old password");
            }
            logger.info("Old password verified for user ID: {}", userId);

            if (newPassword != null && !newPassword.isEmpty()) {
                String encodedNewPassword = passwordEncoder.encode(newPassword);
                existingUser.setPassword(encodedNewPassword);
                logger.info("New password encoded and set for user ID: {}", userId);
            } else {
                logger.warn("New password is null or empty, password not updated for user ID: {}", userId);
                throw new InvalidPasswordException("New password cannot be empty when changing password");
            }
        } else if (newPassword != null && !newPassword.isEmpty()) {
            logger.warn("New password provided without old password for user ID: {}", userId);
            throw new InvalidPasswordException("Old password must be provided to change password");
        }

        // Update other fields if provided
        if (updatedUser.getUsername() != null && !updatedUser.getUsername().isEmpty()) {
            existingUser.setUsername(updatedUser.getUsername());
            logger.info("Username updated for user ID: {}", userId);
        }

        if (updatedUser.getEmail() != null && !updatedUser.getEmail().isEmpty()) {
            existingUser.setEmail(updatedUser.getEmail());
            logger.info("Email updated for user ID: {}", userId);
        }

        if (updatedUser.getName() != null && !updatedUser.getName().isEmpty()) {
            existingUser.setName(updatedUser.getName());
            logger.info("Name updated for user ID: {}", userId);
        }

        if (updatedUser.getPosition() != null && !updatedUser.getPosition().isEmpty()) {
            existingUser.setPosition(updatedUser.getPosition());
            logger.info("Position updated for user ID: {}", userId);
        }

        // Save the updated user
        User savedUser = userRepository.save(existingUser);
        logger.info("User with ID: {} successfully updated and saved", userId);

        return savedUser;
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
}
