package ppl.momofin.momofinbackend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import ppl.momofin.momofinbackend.error.*;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ppl.momofin.momofinbackend.repository.OrganizationRepository;
import ppl.momofin.momofinbackend.repository.UserRepository;
import ppl.momofin.momofinbackend.utility.Roles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    Organization momofin;

    Organization otherOrganization;

    List<User> momofinUsers;

    List<User> otherOrganizationUsers;

    @BeforeEach
    void setup() {
        momofin = new Organization("Momofin");
        User user1 = new User(momofin, "Momofin Financial Samuel","Samuel", "samuel@gmail.com", "encodedMy#Money9078", "Finance Manager");
        User user2 = new User(momofin, "Momofin CEO Darrel", "Darrel Hoei", "darellhoei@gmail.com", "encodedHisPassword#6768", "Co-Founder", true);
        User user3 = new User(momofin, "Momofin Admin Alex", "Alex", "alex@outlook.com", "encodedAlex&Password0959", "Admin", new Roles(true,true));

        momofinUsers = new ArrayList<User>();
        momofinUsers.add(user1);
        momofinUsers.add(user2);
        momofinUsers.add(user3);

        otherOrganization = new Organization("Dondozo");
        User user4 = new User(otherOrganization, "Dondozo Intern Ron", "Ron", "temp-intern@yahoo.com", "encoded123456", "Intern");
        User user5 = new User(otherOrganization, "Dondozo Commander Tatsugiri","Tatsugiri", "commander@email.com", "encodedToxic%Mouth", "Commander", true);

        otherOrganizationUsers = new ArrayList<User>();
        otherOrganizationUsers.add(user4);
        otherOrganizationUsers.add(user5);
    }

    @Test
    void testSaveUser() {
        User userToSave = otherOrganizationUsers.getFirst();

        when(userRepository.save(userToSave)).thenReturn(userToSave);

        User userSaved = userService.save(userToSave);

        assertEquals(userToSave, userSaved);
        verify(userRepository, times(1)).save(userToSave);
    }

    @Test
    void testAuthenticateValid() {
        User userToAuthenticate = otherOrganizationUsers.getFirst();
        String username = userToAuthenticate.getUsername();
        String encryptedPassword = userToAuthenticate.getPassword();
        String password = "123456";
        String organizationName = otherOrganization.getName();

        when(organizationRepository.findOrganizationByName(organizationName)).thenReturn(Optional.of(otherOrganization));
        when(userRepository.findUserByOrganizationAndUsername(
                otherOrganization,
                username
        )).thenReturn(Optional.of(userToAuthenticate));

        when(passwordEncoder.matches(password, encryptedPassword)).thenReturn(true);

        User authenticatedUser = userService.authenticate(
                organizationName,
                username,
                password);

        assertEquals(userToAuthenticate, authenticatedUser);
        assertEquals(username, authenticatedUser.getUsername());
        assertEquals(encryptedPassword, authenticatedUser.getPassword());
        assertNotEquals(password, authenticatedUser.getPassword());

        verify(organizationRepository, times(1))
                .findOrganizationByName(organizationName);
        verify(userRepository, times(1))
                .findUserByOrganizationAndUsername(otherOrganization, username);
        verify(passwordEncoder, times(1)).matches(password, encryptedPassword);
    }

    @Test
    void testAuthenticateInvalidOrganizationName() {
        String organizationName = "invalid";
        when(organizationRepository.findOrganizationByName(organizationName)).thenReturn(Optional.empty());


        OrganizationNotFoundException error = assertThrows(OrganizationNotFoundException.class,
                () -> userService.authenticate(organizationName, "email", "password"));

        assertEquals("The organization "+ organizationName + " is not registered to our database", error.getMessage());

        verify(organizationRepository, times(1))
                .findOrganizationByName(organizationName);
        verify(userRepository, never()).findUserByOrganizationAndUsername(any(Organization.class), anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testAuthenticateIncorrectUsername() {
        String username = "Wrong Username";
        String password = "123456";
        String organizationName = otherOrganization.getName();

        when(organizationRepository.findOrganizationByName(organizationName)).thenReturn(Optional.of(otherOrganization));
        when(userRepository.findUserByOrganizationAndUsername(
                otherOrganization,
                username
        )).thenReturn(Optional.empty());

        InvalidCredentialsException error = assertThrows(InvalidCredentialsException.class,
                () -> userService.authenticate(organizationName, username, password));

        assertEquals("Your email or password is incorrect", error.getMessage());

        verify(organizationRepository, times(1))
                .findOrganizationByName(organizationName);
        verify(userRepository, times(1))
                .findUserByOrganizationAndUsername(otherOrganization, username);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testAuthenticateIncorrectPassword() {
        User userToAuthenticate = otherOrganizationUsers.getFirst();
        String username = userToAuthenticate.getUsername();
        String encryptedPassword = userToAuthenticate.getPassword();
        String password = "Wrong Password";
        String organizationName = otherOrganization.getName();

        when(organizationRepository.findOrganizationByName(organizationName)).thenReturn(Optional.of(otherOrganization));
        when(userRepository.findUserByOrganizationAndUsername(
                otherOrganization,
                username
        )).thenReturn(Optional.of(userToAuthenticate));

        when(passwordEncoder.matches(password, encryptedPassword)).thenReturn(false);

        InvalidCredentialsException error = assertThrows(InvalidCredentialsException.class,
                () -> userService.authenticate(organizationName, username, password));

        assertEquals("Your email or password is incorrect", error.getMessage());

        verify(organizationRepository, times(1))
                .findOrganizationByName(organizationName);
        verify(userRepository, times(1))
                .findUserByOrganizationAndUsername(otherOrganization, username);
        verify(passwordEncoder, times(1)).matches(password, encryptedPassword);
    }

    @Test
    void testRegisterMember() {
        User userToBeRegistered = momofinUsers.getFirst();
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
        User userToBeRegistered = momofinUsers.getFirst();
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
        User userToBeRegistered = momofinUsers.getFirst();
        String username = userToBeRegistered.getUsername();
        String name = userToBeRegistered.getName();
        String email = userToBeRegistered.getEmail();
        String password = userToBeRegistered.getPassword();
        String position = userToBeRegistered.getPosition();

        User existingUser = new User();
        existingUser.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class,
                () -> userService.registerMember(momofin, username, name, email, password, position));

        verify(userRepository, times(1)).findByUsername(username);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        assertEquals("The username "+username+" is already in use", exception.getMessage());
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
        momofinUsers.addAll(otherOrganizationUsers);
        when(userRepository.findAll()).thenReturn(momofinUsers);

        List<User> fetchedUsers = userService.fetchAllUsers();

        assertEquals(momofinUsers, fetchedUsers);
        verify(userRepository, times(1)).findAll();
    }


    @Test
    void getUserById_ReturnsUser_WhenUserExists() {
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setUserId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        User result = userService.getUserById(userId);

        assertEquals(mockUser, result);
    }

    @Test
    void getUserById_ThrowsException_WhenUserDoesNotExist() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(userId));
    }

    @Test
    void updateUser_ReturnsUpdatedUser_WhenUpdateIsSuccessful() {
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setUserId(userId);
        existingUser.setEmail("old@example.com");

        User updatedUser = new User();
        updatedUser.setEmail("new@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(userId, updatedUser);

        assertEquals("new@example.com", result.getEmail());
    }
    @Test
    void updateUser_UpdatesAllFields_WhenAllFieldsProvided() {
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setUserId(userId);
        existingUser.setEmail("old@example.com");
        existingUser.setUsername("oldUsername");
        existingUser.setPassword("oldPassword");

        User updatedUser = new User();
        updatedUser.setEmail("new@example.com");
        updatedUser.setUsername("newUsername");
        updatedUser.setPassword("newPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(userId, updatedUser);

        assertEquals("new@example.com", result.getEmail());
        assertEquals("newUsername", result.getUsername());
        assertEquals("encodedNewPassword", result.getPassword());
        verify(passwordEncoder).encode("newPassword");
    }

    @Test
    void updateUser_UpdatesOnlyProvidedFields_WhenSomeFieldsAreNull() {
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setUserId(userId);
        existingUser.setEmail("old@example.com");
        existingUser.setUsername("oldUsername");
        existingUser.setPassword("oldPassword");

        User updatedUser = new User();
        updatedUser.setEmail("new@example.com");
        // username and password are not provided (null)

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(userId, updatedUser);

        assertEquals("new@example.com", result.getEmail());
        assertEquals("oldUsername", result.getUsername());
        assertEquals("oldPassword", result.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void updateUser_ThrowsUserNotFoundException_WhenUserDoesNotExist() {
        Long userId = 1L;
        User updatedUser = new User();
        updatedUser.setEmail("new@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(userId, updatedUser));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_ValidatesPassword_WhenNewPasswordProvided() {
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setUserId(userId);
        existingUser.setPassword("oldPassword");

        User updatedUser = new User();
        updatedUser.setPassword("invalidPw"); // Assuming this is an invalid password

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        assertThrows(InvalidPasswordException.class, () -> userService.updateUser(userId, updatedUser));
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void fetchUserSuccess() {
        User user = momofinUsers.getFirst();
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        User fetchedUser = userService.fetchUserByUsername(user.getUsername());

        assertEquals(user, fetchedUser);
        verify(userRepository).findByUsername(user.getUsername());
    }

    @Test
    void fetchUserNull () {
        when(userRepository.findByUsername("invalid user")).thenReturn(Optional.empty());

        User fetchedUser = userService.fetchUserByUsername("invalid user");

        assertNull(fetchedUser);
        verify(userRepository).findByUsername("invalid user");
    }

    @Test
    void registerOrganizationAdmin_shouldCreateAndReturnNewAdminUser() {
        // Arrange
        Organization org = new Organization("Org", "Desc");
        User newUser = new User(org, "admin", "Admin Name", null, "encodedPassword", null);
        newUser.setOrganizationAdmin(true);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // Act
        User result = userService.registerOrganizationAdmin(org, "admin", "Admin Name", null, "validPassword123", null);

        // Assert
        assertNotNull(result);
        assertEquals("admin", result.getUsername());
        assertEquals("Admin Name", result.getName());
        assertNull(result.getEmail());
        assertNull(result.getPosition());
        assertTrue(result.isOrganizationAdmin());
        verify(passwordEncoder).encode("validPassword123");
    }
    @Test
    void registerOrganizationAdmin_shouldThrowException_whenAdminUsernameAlreadyExists() {
        // Arrange
        Organization org = new Organization("Org", "Desc");
        String existingUsername = "existingAdmin";
        when(userRepository.findByUsername(existingUsername)).thenReturn(Optional.of(new User()));

        // Act & Assert
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () ->
                userService.registerOrganizationAdmin(org, existingUsername, "Admin Name", null, "validPassword123", null)
        );

        assertEquals("An admin with this username already exists", exception.getMessage());
    }
}