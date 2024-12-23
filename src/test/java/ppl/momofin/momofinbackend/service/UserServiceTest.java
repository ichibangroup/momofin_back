package ppl.momofin.momofinbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ppl.momofin.momofinbackend.error.InvalidCredentialsException;
import ppl.momofin.momofinbackend.error.InvalidPasswordException;
import ppl.momofin.momofinbackend.error.OrganizationNotFoundException;
import ppl.momofin.momofinbackend.error.UserAlreadyExistsException;
import ppl.momofin.momofinbackend.error.UserNotFoundException;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.repository.OrganizationRepository;
import ppl.momofin.momofinbackend.repository.UserRepository;
import ppl.momofin.momofinbackend.utility.Roles;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private Organization momofin;
    private Organization otherOrganization;
    private List<User> momofinUsers;
    private List<User> otherOrganizationUsers;
    private User initialUser;
    private UUID userId;

    @BeforeEach
    void setup() {
        momofin = new Organization("Momofin");
        User user1 = new User(momofin, "Momofin Financial Samuel", "Samuel", "samuel@gmail.com", "encodedMy#Money9078", "Finance Manager");
        User user2 = new User(momofin, "Momofin CEO Darrel", "Darrel Hoei", "darellhoei@gmail.com", "encodedHisPassword#6768", "Co-Founder", true);
        User user3 = new User(momofin, "Momofin Admin Alex", "Alex", "alex@outlook.com", "encodedAlex&Password0959", "Admin", new Roles(true, true));

        momofinUsers = new ArrayList<>();
        momofinUsers.add(user1);
        momofinUsers.add(user2);
        momofinUsers.add(user3);

        otherOrganization = new Organization("Dondozo");
        User user4 = new User(otherOrganization, "Dondozo Intern Ron", "Ron", "temp-intern@yahoo.com", "encoded123456", "Intern");
        User user5 = new User(otherOrganization, "Dondozo Commander Tatsugiri", "Tatsugiri", "commander@email.com", "encodedToxic%Mouth", "Commander", true);

        otherOrganizationUsers = new ArrayList<>();
        otherOrganizationUsers.add(user4);
        otherOrganizationUsers.add(user5);

        initialUser = new User();
        userId = UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe");
        initialUser.setUserId(userId);
        initialUser.setEmail("old@example.com");
        initialUser.setUsername("oldUsername");
        initialUser.setPassword("encodedOldPassword");
    }

    @Test
    void testSaveUser() {
        User userToSave = otherOrganizationUsers.get(0);
        when(userRepository.save(userToSave)).thenReturn(userToSave);

        User userSaved = userService.save(userToSave);

        assertEquals(userToSave, userSaved);
        verify(userRepository, times(1)).save(userToSave);
    }

    @Test
    void testAuthenticateValid() {
        User userToAuthenticate = otherOrganizationUsers.get(0);
        String username = userToAuthenticate.getUsername();
        String encryptedPassword = userToAuthenticate.getPassword();
        String password = "123456";
        String organizationName = otherOrganization.getName();

        when(organizationRepository.findOrganizationByName(organizationName)).thenReturn(Optional.of(otherOrganization));
        when(userRepository.findUserByOrganizationAndUsername(otherOrganization, username)).thenReturn(Optional.of(userToAuthenticate));
        when(passwordEncoder.matches(password, encryptedPassword)).thenReturn(true);

        User authenticatedUser = userService.authenticate(organizationName, username, password);

        assertEquals(userToAuthenticate, authenticatedUser);
        assertEquals(username, authenticatedUser.getUsername());
        assertEquals(encryptedPassword, authenticatedUser.getPassword());
        assertNotEquals(password, authenticatedUser.getPassword());

        verify(organizationRepository, times(1)).findOrganizationByName(organizationName);
        verify(userRepository, times(1)).findUserByOrganizationAndUsername(otherOrganization, username);
        verify(passwordEncoder, times(1)).matches(password, encryptedPassword);
    }
    @Test
    void testAuthenticateInvalidOrganizationName() {
        String organizationName = "invalid";
        when(organizationRepository.findOrganizationByName(organizationName)).thenReturn(Optional.empty());

        OrganizationNotFoundException error = assertThrows(OrganizationNotFoundException.class,
                () -> userService.authenticate(organizationName, "email", "password"));

        assertEquals("The organization " + organizationName + " is not registered to our database", error.getMessage());

        verify(organizationRepository, times(1)).findOrganizationByName(organizationName);
        verify(userRepository, never()).findUserByOrganizationAndUsername(any(Organization.class), anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testAuthenticateIncorrectUsername() {
        String username = "Wrong Username";
        String password = "123456";
        String organizationName = otherOrganization.getName();

        when(organizationRepository.findOrganizationByName(organizationName)).thenReturn(Optional.of(otherOrganization));
        when(userRepository.findUserByOrganizationAndUsername(otherOrganization, username)).thenReturn(Optional.empty());

        InvalidCredentialsException error = assertThrows(InvalidCredentialsException.class,
                () -> userService.authenticate(organizationName, username, password));

        assertEquals("Your email or password is incorrect", error.getMessage());

        verify(organizationRepository, times(1)).findOrganizationByName(organizationName);
        verify(userRepository, times(1)).findUserByOrganizationAndUsername(otherOrganization, username);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testAuthenticateIncorrectPassword() {
        User userToAuthenticate = otherOrganizationUsers.get(0);
        String username = userToAuthenticate.getUsername();
        String encryptedPassword = userToAuthenticate.getPassword();
        String password = "Wrong Password";
        String organizationName = otherOrganization.getName();

        when(organizationRepository.findOrganizationByName(organizationName)).thenReturn(Optional.of(otherOrganization));
        when(userRepository.findUserByOrganizationAndUsername(otherOrganization, username)).thenReturn(Optional.of(userToAuthenticate));
        when(passwordEncoder.matches(password, encryptedPassword)).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> userService.authenticate(organizationName, username, password));

        verify(organizationRepository, times(1)).findOrganizationByName(organizationName);
        verify(userRepository, times(1)).findUserByOrganizationAndUsername(otherOrganization, username);
        verify(passwordEncoder, times(1)).matches(password, encryptedPassword);
    }

    @Test
    void testRegisterMember() {
        User userToBeRegistered = momofinUsers.get(0);
        String username = userToBeRegistered.getUsername();
        String name = userToBeRegistered.getName();
        String email = userToBeRegistered.getEmail();
        String encryptedPassword = userToBeRegistered.getPassword();
        String password = "My#Money9078";
        String position = userToBeRegistered.getPosition();

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(encryptedPassword);
        when(userRepository.save(any(User.class))).thenReturn(userToBeRegistered);

        User registeredUser = userService.registerMember(momofin, username, name, email, password, position);

        assertEquals(userToBeRegistered, registeredUser);
        assertNotEquals(password, registeredUser.getPassword());
        verify(userRepository, times(1)).findByUsername(username);
        verify(passwordEncoder, times(1)).encode(password);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterMemberPasswordTooShort() {
        User userToBeRegistered = momofinUsers.get(0);
        String username = userToBeRegistered.getUsername();
        String name = userToBeRegistered.getName();
        String email = userToBeRegistered.getEmail();
        String password = "Thisistoo";
        String position = userToBeRegistered.getPosition();

        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class,
                () -> userService.registerMember(momofin, username, name, email, password, position));

        verify(userRepository, never()).findByUsername(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));

        assertEquals("Password must be at least 10 characters long.", exception.getMessage());
    }
    @Test
    void testRegisterMemberUsernameInUse() {
        User userToBeRegistered = momofinUsers.get(0);
        String username = userToBeRegistered.getUsername();
        String name = userToBeRegistered.getName();
        String email = userToBeRegistered.getEmail();
        String password = userToBeRegistered.getPassword();
        String position = userToBeRegistered.getPosition();

        User userUnderTest = new User();
        userUnderTest.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userUnderTest));

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class,
                () -> userService.registerMember(momofin, username, name, email, password, position));

        verify(userRepository, times(1)).findByUsername(username);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        assertEquals("The username " + username + " is already in use", exception.getMessage());
    }

    @Test
    void testFetchAllOrganizationalUsers() {
        when(userRepository.findAllByOrganization(momofin)).thenReturn(momofinUsers);

        List<User> fetchedUsers = userService.fetchUsersByOrganization(momofin);

        assertEquals(momofinUsers, fetchedUsers);
        verify(userRepository, times(1)).findAllByOrganization(momofin);
    }

    @Test
    void testFetchAllUsers() {
        // Setup test data ensuring each user has an ID that's not -1
        momofinUsers.forEach(user -> user.setUserId(userId));  // Or use different IDs if needed

        List<User> allUsers = new ArrayList<>(momofinUsers);
        allUsers.addAll(otherOrganizationUsers);

        // Add the deleted user (ID -1) to verify it gets filtered out
        User deletedUser = new User();
        deletedUser.setUsername("deleted_user");
        allUsers.add(deletedUser);

        when(userRepository.findAll()).thenReturn(allUsers);

        List<User> fetchedUsers = userService.fetchAllUsers();

        // Verify the result doesn't include the deleted user
        assertEquals(momofinUsers.size() + otherOrganizationUsers.size(), fetchedUsers.size());
        assertFalse(fetchedUsers.stream().anyMatch(user -> user.getUsername().equals("deleted_user")));
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_ReturnsUser_WhenUserExists() {
        User mockUser = new User();
        mockUser.setUserId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        User result = userService.getUserById(userId);

        assertEquals(mockUser, result);
    }

    @Test
    void getUserById_ThrowsException_WhenUserDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(userId));
    }

    @Test
    void updateUser_UpdatesUsername() {
        String newUsername = "NewUsername";

        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setUsername("oldUsername");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User updatedUser = new User();
        updatedUser.setUsername(newUsername);
        User result = userService.updateUser(userId, updatedUser, null, null);

        assertEquals(newUsername, result.getUsername());
        verify(userRepository).save(any(User.class));
    }
    @Test
    void updateUser_UpdatesEmail() {
        String newEmail = "new@example.com";

        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setEmail("oldEmail");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User updatedUser = new User();
        updatedUser.setEmail(newEmail);
        User result = userService.updateUser(userId, updatedUser, null, null);

        assertEquals(newEmail, result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_UpdatesName() {
        String newName = "New Name";

        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setName("oldName");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User updatedUser = new User();
        updatedUser.setName(newName);
        User result = userService.updateUser(userId, updatedUser, null, null);

        assertEquals(newName, result.getName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_UpdatesPosition() {
        String newPosition = "New Position";

        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setPosition("oldPosition");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User updatedUser = new User();
        updatedUser.setPosition(newPosition);
        User result = userService.updateUser(userId, updatedUser, null, null);

        assertEquals(newPosition, result.getPosition());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_SuccessfulPasswordUpdate() {
        String oldPassword = "VeryPowrfulPassword.com";
        String newPassword = "NewStrongPassword123";

        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setPassword(passwordEncoder.encode(oldPassword));

        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));
        when(passwordEncoder.matches(oldPassword, userUnderTest.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User result = userService.updateUser(userId, new User(), oldPassword, newPassword);

        assertEquals("encodedNewPassword", result.getPassword());
        verify(passwordEncoder).encode(newPassword);
    }

    @Test
    void updateUser_NewPasswordIsNullOrEmpty_ThrowsInvalidPasswordException() {
        User updatedUser = new User();

        when(userRepository.findById(userId)).thenReturn(Optional.of(initialUser));

        assertThrows(InvalidPasswordException.class,
                () -> userService.updateUser(userId, updatedUser, "oldPassword", null));

        assertThrows(InvalidPasswordException.class,
                () -> userService.updateUser(userId, updatedUser, "oldPassword", ""));
    }
    @Test
    void updateUser_InvalidOldPassword_ThrowsInvalidPasswordException() {
        User updatedUser = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(initialUser));
        when(passwordEncoder.matches("wrongPassword", "encodedOldPassword")).thenReturn(false);

        assertThrows(InvalidPasswordException.class,
                () -> userService.updateUser(userId, updatedUser, "wrongPassword", "newPassword"));
    }

    @Test
    void updateUser_NewPasswordWithoutOldPassword_ThrowsInvalidPasswordException() {
        User updatedUser = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(initialUser));

        assertThrows(InvalidPasswordException.class,
                () -> userService.updateUser(userId, updatedUser, null, "newPassword"));
    }

    @Test
    void updateUser_NoPasswordChange_UpdatesOtherFields() {
        User updatedUser = new User();
        updatedUser.setUsername("newUsername");
        updatedUser.setEmail("new@example.com");

        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setUsername("oldUsername");
        userUnderTest.setEmail("oldEmail");
        userUnderTest.setPassword("encodedOldPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.updateUser(userId, updatedUser, null, null);

        assertNotNull(result);
        assertEquals("newUsername", result.getUsername());
        assertEquals("new@example.com", result.getEmail());
        assertEquals("encodedOldPassword", result.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_OldPasswordProvidedNewPasswordNull_ThrowsInvalidPasswordException() {
        User updatedUser = new User();
        String oldPassword = "OldPassword123";

        when(userRepository.findById(userId)).thenReturn(Optional.of(initialUser));
        when(passwordEncoder.matches(oldPassword, initialUser.getPassword())).thenReturn(true);

        assertThrows(InvalidPasswordException.class,
                () -> userService.updateUser(userId, updatedUser, oldPassword, null));
    }

    @Test
    void updateUser_UpdatesAllFields() {
        User updatedUser = new User();
        updatedUser.setUsername("newUsername");
        updatedUser.setEmail("new@example.com");
        updatedUser.setName("New Name");
        updatedUser.setPosition("New Position");

        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setUsername("oldUsername");
        userUnderTest.setEmail("old@example.com");
        userUnderTest.setName("Old Name");
        userUnderTest.setPosition("Old Position");
        userUnderTest.setPassword(passwordEncoder.encode("oldPassword"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));
        when(passwordEncoder.matches("oldPassword", userUnderTest.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.updateUser(userId, updatedUser, "oldPassword", "newPassword");

        assertEquals("newUsername", result.getUsername());
        assertEquals("new@example.com", result.getEmail());
        assertEquals("New Name", result.getName());
        assertEquals("New Position", result.getPosition());
        assertEquals("encodedNewPassword", result.getPassword());
    }

    @Test
    void fetchUserByUsername_UserExists() {
        String username = "existingUser";
        User user = new User();
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        User result = userService.fetchUserByUsername(username);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
    }

    @Test
    void fetchUserByUsername_UserDoesNotExist() {
        String username = "nonExistingUser";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        User result = userService.fetchUserByUsername(username);

        assertNull(result);
    }

    @Test
    void registerOrganizationAdmin_Success() {
        Organization organization = new Organization("TestOrg");
        String username = "admin";
        String name = "Admin Name";
        String email = "admin@example.com";
        String password = "AdminPassword123";
        String position = "Admin";

        User newUser = new User(organization, username, name, email, password, position);
        newUser.setOrganizationAdmin(true);

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        User result = userService.registerOrganizationAdmin(organization, username, name, email, password, position);

        assertNotNull(result);
        assertTrue(result.isOrganizationAdmin());
        assertEquals(username, result.getUsername());
        assertEquals(name, result.getName());
        assertEquals(email, result.getEmail());
        assertEquals(position, result.getPosition());
    }
    @Test
    void registerOrganizationAdmin_UsernameAlreadyExists() {
        Organization organization = new Organization("TestOrg");
        String username = "existingAdmin";
        String name = "Admin Name";
        String email = "admin@example.com";
        String password = "AdminPassword123";
        String position = "Admin";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(new User()));

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.registerOrganizationAdmin(organization, username, name, email, password, position));
    }

    @Test
    void updateUser_OnlyUsernameProvided() {
        User updatedUser = new User();
        updatedUser.setUsername("newUsername");

        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setUsername("oldUsername");
        userUnderTest.setEmail("old@example.com");
        userUnderTest.setName("Old Name");
        userUnderTest.setPosition("Old Position");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.updateUser(userId, updatedUser, null, null);

        assertEquals("newUsername", result.getUsername());
        assertEquals("old@example.com", result.getEmail());
        assertEquals("Old Name", result.getName());
        assertEquals("Old Position", result.getPosition());
    }

    @Test
    void updateUser_OnlyEmailProvided() {
        User updatedUser = new User();
        updatedUser.setEmail("new@example.com");

        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setUsername("oldUsername");
        userUnderTest.setEmail("old@example.com");
        userUnderTest.setName("Old Name");
        userUnderTest.setPosition("Old Position");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.updateUser(userId, updatedUser, null, null);

        assertEquals("oldUsername", result.getUsername());
        assertEquals("new@example.com", result.getEmail());
        assertEquals("Old Name", result.getName());
        assertEquals("Old Position", result.getPosition());
    }

    @Test
    void updateUser_OnlyNameProvided() {
        User updatedUser = new User();
        updatedUser.setName("New Name");

        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setUsername("oldUsername");
        userUnderTest.setEmail("old@example.com");
        userUnderTest.setName("Old Name");
        userUnderTest.setPosition("Old Position");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.updateUser(userId, updatedUser, null, null);

        assertEquals("oldUsername", result.getUsername());
        assertEquals("old@example.com", result.getEmail());
        assertEquals("New Name", result.getName());
        assertEquals("Old Position", result.getPosition());
    }
    @Test
    void updateUser_OnlyPositionProvided() {
        User updatedUser = new User();
        updatedUser.setPosition("New Position");

        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setUsername("oldUsername");
        userUnderTest.setEmail("old@example.com");
        userUnderTest.setName("Old Name");
        userUnderTest.setPosition("Old Position");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.updateUser(userId, updatedUser, null, null);

        assertEquals("oldUsername", result.getUsername());
        assertEquals("old@example.com", result.getEmail());
        assertEquals("Old Name", result.getName());
        assertEquals("New Position", result.getPosition());
    }

    @Test
    void updateUser_NoFieldsProvided_NoChanges() {
        User updatedUser = new User();

        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setUsername("oldUsername");
        userUnderTest.setEmail("old@example.com");
        userUnderTest.setName("Old Name");
        userUnderTest.setPosition("Old Position");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.updateUser(userId, updatedUser, null, null);

        assertEquals("oldUsername", result.getUsername());
        assertEquals("old@example.com", result.getEmail());
        assertEquals("Old Name", result.getName());
        assertEquals("Old Position", result.getPosition());
    }

    @Test
    void updateUser_PasswordChangeWithoutOtherFields() {
        User updatedUser = new User();
        String oldPassword = "OldPassword123";
        String newPassword = "NewPassword123";

        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setPassword(passwordEncoder.encode(oldPassword));

        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));
        when(passwordEncoder.matches(oldPassword, userUnderTest.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.updateUser(userId, updatedUser, oldPassword, newPassword);

        assertEquals("encodedNewPassword", result.getPassword());
        verify(passwordEncoder).encode(newPassword);
    }

    @Test
    void updateUser_NullFields() {
        User updatedUser = new User();
        updatedUser.setUsername(null);
        updatedUser.setEmail(null);
        updatedUser.setName(null);
        updatedUser.setPosition(null);

        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setUsername("oldUsername");
        userUnderTest.setEmail("old@example.com");
        userUnderTest.setName("Old Name");
        userUnderTest.setPosition("Old Position");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.updateUser(userId, updatedUser, null, null);

        assertEquals("oldUsername", result.getUsername());
        assertEquals("old@example.com", result.getEmail());
        assertEquals("Old Name", result.getName());
        assertEquals("Old Position", result.getPosition());
    }
    @Test
    void updateUser_OnlyOldPasswordProvided_ThrowsInvalidPasswordException() {
        User updatedUser = new User();
        String oldPassword = "OldPassword123";

        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setUsername("oldUsername");
        userUnderTest.setEmail("old@example.com");
        userUnderTest.setPassword(passwordEncoder.encode(oldPassword));

        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));
        when(passwordEncoder.matches(oldPassword, userUnderTest.getPassword())).thenReturn(true);

        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class,
                () -> userService.updateUser(userId, updatedUser, oldPassword, null));

        assertEquals("New password cannot be empty when changing password", exception.getMessage());
    }

    @Test
    void updateUser_EmptyFieldsProvided() {
        User updatedUser = new User();
        updatedUser.setUsername("");
        updatedUser.setEmail("");
        updatedUser.setName("");
        updatedUser.setPosition("");

        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setUsername("oldUsername");
        userUnderTest.setEmail("old@example.com");
        userUnderTest.setName("Old Name");
        userUnderTest.setPosition("Old Position");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.updateUser(userId, updatedUser, null, null);

        assertEquals("oldUsername", result.getUsername());
        assertEquals("old@example.com", result.getEmail());
        assertEquals("Old Name", result.getName());
        assertEquals("Old Position", result.getPosition());
    }

    @Test
    void updateUser_NewPasswordProvidedWithEmptyOldPassword_ThrowsInvalidPasswordException() {
        User updatedUser = new User(); // Empty user object
        String oldPassword = ""; // Empty string instead of null
        String newPassword = "NewPassword123";

        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setUsername("existingUsername");
        userUnderTest.setPassword("existingEncodedPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));

        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class,
                () -> userService.updateUser(userId, updatedUser, oldPassword, newPassword));

        assertEquals("Old password must be provided to change password", exception.getMessage());

        verify(userRepository).findById(userId);
        verifyNoInteractions(passwordEncoder);
        verifyNoMoreInteractions(userRepository);
    }
    @Test
    void updateUser_OnlyPasswordUpdate_SuccessfulUpdate() {
        User updatedUser = new User(); // No fields set
        String oldPassword = "OldPassword123";
        String newPassword = "NewPassword123";

        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setUsername("existingUsername");
        userUnderTest.setEmail("existing@example.com");
        userUnderTest.setName("Existing Name");
        userUnderTest.setPosition("Existing Position");
        userUnderTest.setPassword("encodedOldPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));
        when(passwordEncoder.matches(oldPassword, "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.updateUser(userId, updatedUser, oldPassword, newPassword);

        assertNotNull(result);
        assertEquals("existingUsername", result.getUsername());
        assertEquals("existing@example.com", result.getEmail());
        assertEquals("Existing Name", result.getName());
        assertEquals("Existing Position", result.getPosition());
        assertEquals("encodedNewPassword", result.getPassword());

        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches(oldPassword, "encodedOldPassword");
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(any(User.class));
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    void updateUser_NewPasswordWithEmptyOldPassword_ThrowsInvalidPasswordException() {
        User updatedUser = new User(); // No fields set
        String newPassword = "NewPassword123";
        String oldPassword = ""; // Empty string instead of null

        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setUsername("existingUsername");
        userUnderTest.setEmail("existing@example.com");
        userUnderTest.setName("Existing Name");
        userUnderTest.setPosition("Existing Position");
        userUnderTest.setPassword("existingEncodedPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));

        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class,
                () -> userService.updateUser(userId, updatedUser, oldPassword, newPassword));

        assertEquals("Old password must be provided to change password", exception.getMessage());

        verify(userRepository).findById(userId);
        verifyNoInteractions(passwordEncoder);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUser_NewPasswordProvidedOldPasswordNullUpdatedUserNull_ThrowsInvalidPasswordException() {
        User updatedUser = null; // Set updatedUser to null
        String oldPassword = null;
        String newPassword = "NewPassword123";

        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setUsername("existingUsername");
        userUnderTest.setPassword("existingEncodedPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));

        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class,
                () -> userService.updateUser(userId, updatedUser, oldPassword, newPassword));

        assertEquals("Old password must be provided to change password", exception.getMessage());

        verify(userRepository).findById(userId);
        verifyNoInteractions(passwordEncoder);
        verifyNoMoreInteractions(userRepository);
    }
    @Test
    void updateUser_NewPasswordProvidedWithoutOldPassword_ThrowsInvalidPasswordException() {
        User updatedUser = new User(); // Empty user object
        String oldPassword = null; // Explicitly set to null
        String newPassword = "NewPassword123";

        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setUsername("existingUsername");
        userUnderTest.setPassword("existingEncodedPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));

        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class,
                () -> userService.updateUser(userId, updatedUser, oldPassword, newPassword));

        assertEquals("Old password must be provided to change password", exception.getMessage());

        verify(userRepository).findById(userId);
        verifyNoInteractions(passwordEncoder);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUser_NewPasswordProvidedWithoutOldPasswordAllFieldsNull_ThrowsInvalidPasswordException() {
        User updatedUser = new User();
        updatedUser.setUsername(null);
        updatedUser.setEmail(null);
        updatedUser.setName(null);
        updatedUser.setPosition(null);
        String oldPassword = null;
        String newPassword = "NewPassword123";

        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setUsername("existingUsername");
        userUnderTest.setEmail("existing@email.com");
        userUnderTest.setName("Existing Name");
        userUnderTest.setPosition("Existing Position");
        userUnderTest.setPassword("existingEncodedPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));

        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class,
                () -> userService.updateUser(userId, updatedUser, oldPassword, newPassword));

        assertEquals("Old password must be provided to change password", exception.getMessage());

        verify(userRepository).findById(userId);
        verifyNoInteractions(passwordEncoder);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUser_NoChanges_StillSavesUser() {
        User updatedUser = new User(); // Empty user object
        String oldPassword = null;
        String newPassword = null;

        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setUsername("existingUsername");
        userUnderTest.setEmail("existing@email.com");
        userUnderTest.setName("Existing Name");
        userUnderTest.setPosition("Existing Position");
        userUnderTest.setPassword("existingEncodedPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));
        when(userRepository.save(any(User.class))).thenReturn(userUnderTest);

        User result = userService.updateUser(userId, updatedUser, oldPassword, newPassword);

        assertNotNull(result);
        assertEquals("existingUsername", result.getUsername());
        assertEquals("existing@email.com", result.getEmail());
        assertEquals("Existing Name", result.getName());
        assertEquals("Existing Position", result.getPosition());
        assertEquals("existingEncodedPassword", result.getPassword());

        verify(userRepository).findById(userId);
        verify(userRepository).save(userUnderTest);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void updateUser_NewPasswordEdgeCases() {
        User updatedUser = new User();
        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setPassword("existingEncodedPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Case 1: newPassword is null
        User result1 = userService.updateUser(userId, updatedUser, null, null);
        assertEquals("existingEncodedPassword", result1.getPassword());

        // Case 2: newPassword is empty string
        User result2 = userService.updateUser(userId, updatedUser, null, "");
        assertEquals("existingEncodedPassword", result2.getPassword());

        // Case 3: newPassword is not null and not empty
        assertThrows(InvalidPasswordException.class,
                () -> userService.updateUser(userId, updatedUser, null, "newPassword"));

        // Case 4: newPassword and oldPassword both provided
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        User result4 = userService.updateUser(userId, updatedUser, "oldPassword", "newPassword");
        assertEquals("newEncodedPassword", result4.getPassword());

        verify(userRepository, times(4)).findById(userId);
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(3)).save(any(User.class));
    }

    @Test
    void updateUser_UpdatedUserNull_NoChanges() {
        User updatedUser = null;
        String oldPassword = null;
        String newPassword = null;

        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setUsername("existingUsername");
        userUnderTest.setPassword("existingEncodedPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.updateUser(userId, updatedUser, oldPassword, newPassword);

        assertEquals("existingUsername", result.getUsername());
        assertEquals("existingEncodedPassword", result.getPassword());
        verify(userRepository).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }
    @Test
    void updateUser_UnrecognizedField_NoChange() {
        User updatedUser = new User();
        updatedUser.setUsername("newUsername");
        updatedUser.setEmail("new@email.com");
        updatedUser.setName("New Name");
        updatedUser.setPosition("New Position");

        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setUsername("oldUsername");
        userUnderTest.setEmail("old@email.com");
        userUnderTest.setName("Old Name");
        userUnderTest.setPosition("Old Position");

        // Add a method to set an unrecognized field
        setUnrecognizedField(updatedUser, "unrecognizedField", "someValue");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.updateUser(userId, updatedUser, null, null);

        assertEquals("newUsername", result.getUsername());
        assertEquals("new@email.com", result.getEmail());
        assertEquals("New Name", result.getName());
        assertEquals("New Position", result.getPosition());
        // Verify that the unrecognized field didn't cause any issues
        verify(userRepository).save(any(User.class));
    }

    // Helper method to set an unrecognized field using reflection
    private void setUnrecognizedField(User user, String fieldName, String value) {
        try {
            Field field = User.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(user, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Handle exception or ignore for test purposes
        }
    }
    @Test
    void updateField_UnrecognizedField_NoChange() {
        User user = new User();
        user.setUsername("oldUsername");
        user.setEmail("old@email.com");
        user.setName("Old Name");
        user.setPosition("Old Position");

        userService.updateFieldForTesting(user, "unrecognizedField", "someValue");

        assertEquals("oldUsername", user.getUsername());
        assertEquals("old@email.com", user.getEmail());
        assertEquals("Old Name", user.getName());
        assertEquals("Old Position", user.getPosition());
        // Verify that no change occurred for the unrecognized field
    }

    @Test
    void updateUser_OldPasswordNullNewPasswordProvided_ThrowsInvalidPasswordException() {
        User updatedUser = new User();
        String oldPassword = null;
        String newPassword = "NewPassword123";

        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setPassword("existingEncodedPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));

        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class,
                () -> userService.updateUser(userId, updatedUser, oldPassword, newPassword));

        assertEquals("Old password must be provided to change password", exception.getMessage());
        verify(userRepository).findById(userId);
        verifyNoInteractions(passwordEncoder);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUser_OldPasswordProvidedNewPasswordProvided_SuccessfulPasswordChange() {
        User updatedUser = new User();
        String oldPassword = "OldPassword123";
        String newPassword = "NewPassword123";

        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setPassword("encodedOldPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));
        when(passwordEncoder.matches(oldPassword, "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.updateUser(userId, updatedUser, oldPassword, newPassword);

        assertEquals("encodedNewPassword", result.getPassword());
        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches(oldPassword, "encodedOldPassword");
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_OldPasswordProvidedNewPasswordEmpty_ThrowsInvalidPasswordException() {
        User updatedUser = new User();
        String oldPassword = "OldPassword123";
        String newPassword = "";

        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setPassword("encodedOldPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));
        when(passwordEncoder.matches(oldPassword, "encodedOldPassword")).thenReturn(true);

        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class,
                () -> userService.updateUser(userId, updatedUser, oldPassword, newPassword));

        assertEquals("New password cannot be empty when changing password", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches(oldPassword, "encodedOldPassword");
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUser_BothPasswordsNullOrEmpty_NoPasswordChange() {
        User updatedUser = new User();
        updatedUser.setUsername("newUsername");
        String oldPassword = null;
        String newPassword = null;

        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setUsername("oldUsername");
        userUnderTest.setPassword("existingEncodedPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.updateUser(userId, updatedUser, oldPassword, newPassword);

        assertEquals("newUsername", result.getUsername());
        assertEquals("existingEncodedPassword", result.getPassword());
        verify(userRepository).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }
    @Test
    void updateUser_OldPasswordNotNullButEmpty_NoPasswordChange() {
        User updatedUser = new User();
        updatedUser.setUsername("newUsername");
        String oldPassword = ""; // Not null, but empty
        String newPassword = null;

        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setUsername("oldUsername");
        userUnderTest.setPassword("existingEncodedPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.updateUser(userId, updatedUser, oldPassword, newPassword);

        assertEquals("newUsername", result.getUsername());
        assertEquals("existingEncodedPassword", result.getPassword());
        verify(userRepository).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }
    @Test
    void updateUser_OldPasswordNotNullButEmpty_ThrowsInvalidPasswordException() {
        User updatedUser = new User();
        String oldPassword =
                ""; // Not null, but empty
        String newPassword = "NewPassword123";
        User userUnderTest = new User();
        userUnderTest.setUserId(userId);
        userUnderTest.setPassword("existingEncodedPassword");
        when(userRepository.findById(userId)).thenReturn(Optional.of(userUnderTest));
        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class,
                () -> userService.updateUser(userId, updatedUser, oldPassword, newPassword));
        assertEquals("Old password must be provided to change password", exception.getMessage());
        verify(userRepository).findById(userId);
        verifyNoInteractions(passwordEncoder);
        verifyNoMoreInteractions(userRepository);
    }
    @Test
    void updateUser_NewUsernameAlreadyExists_ThrowsUserAlreadyExistsException() {
        // Setup existing user to update
        User existingUser = new User();
        existingUser.setUserId(userId);
        existingUser.setUsername("oldUsername");

        // Setup updated user with new username
        User updatedUser = new User();
        updatedUser.setUsername("existingUsername");

        // Setup another existing user in the system with the desired username
        User userWithDesiredUsername = new User();
        userWithDesiredUsername.setUsername("existingUsername");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("existingUsername")).thenReturn(Optional.of(userWithDesiredUsername));

        // Test and verify exception
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class,
                () -> userService.updateUser(userId, updatedUser, null, null));

        assertEquals("A user with this username already exists", exception.getMessage());

        // Verify repository calls
        verify(userRepository).findById(userId);
        verify(userRepository).findByUsername("existingUsername");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_NewUsernameAvailable_UpdatesSuccessfully() {
        // Setup existing user to update
        User existingUser = new User();
        existingUser.setUserId(userId);
        existingUser.setUsername("oldUsername");

        // Setup updated user with new username
        User updatedUser = new User();
        updatedUser.setUsername("newUsername");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("newUsername")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Test
        User result = userService.updateUser(userId, updatedUser, null, null);

        // Verify
        assertEquals("newUsername", result.getUsername());
        verify(userRepository).findById(userId);
        verify(userRepository).findByUsername("newUsername");
        verify(userRepository).save(any(User.class));
    }
    @Test
    void updateUser_SameUsernameWithEmailUpdate_UpdatesSuccessfully() {
        // Setup existing user
        User existingUser = new User();
        existingUser.setUserId(userId);
        existingUser.setUsername("existingUsername");
        existingUser.setEmail("old@example.com");

        // Setup updated user with same username but new email
        User updatedUser = new User();
        updatedUser.setUsername("existingUsername");  // Same username
        updatedUser.setEmail("new@example.com");      // New email

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Test
        User result = userService.updateUser(userId, updatedUser, null, null);

        // Verify that email was updated while keeping the same username
        assertEquals("existingUsername", result.getUsername());
        assertEquals("new@example.com", result.getEmail());
        verify(userRepository).findById(userId);
        // Username check should not happen when username hasn't changed
        verify(userRepository, never()).findByUsername(anyString());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_NewUsernameAlreadyExists_ThrowsException() {
        // Setup existing user
        User existingUser = new User();
        existingUser.setUserId(userId);
        existingUser.setUsername("oldUsername");
        existingUser.setEmail("old@example.com");

        // Setup updated user with a username that already exists
        User updatedUser = new User();
        updatedUser.setUsername("takenUsername");  // Different username that's already taken
        updatedUser.setEmail("new@example.com");

        // Setup mock for existing username in database
        User existingUserWithUsername = new User();
        existingUserWithUsername.setUsername("takenUsername");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("takenUsername")).thenReturn(Optional.of(existingUserWithUsername));

        // Test and verify exception is thrown
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class,
                () -> userService.updateUser(userId, updatedUser, null, null));

        assertEquals("A user with this username already exists", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(userRepository).findByUsername("takenUsername");
        verify(userRepository, never()).save(any(User.class));
    }




}
