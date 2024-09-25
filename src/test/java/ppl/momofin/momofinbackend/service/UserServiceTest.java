package ppl.momofin.momofinbackend.service;

import ppl.momofin.momofinbackend.error.InvalidCredentialsException;
import ppl.momofin.momofinbackend.error.InvalidPasswordException;
import ppl.momofin.momofinbackend.error.OrganizationNotFoundException;
import ppl.momofin.momofinbackend.error.UserAlreadyExistsException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    Organization momofin;

    Organization otherOrganization;

    List<User> momofinUsers;

    List<User> otherOrganizationUsers;

    @BeforeEach
    void setup() {
        momofin = new Organization("Momofin");
        User user1 = new User(momofin, "Momofin Financial Samuel","Samuel", "samuel@gmail.com", "My#Money9078", "Finance Manager");
        User user2 = new User(momofin, "Momofin CEO Darrel", "Darrel Hoei", "darellhoei@gmail.com", "HisPassword#6768", "Co-Founder", true);
        User user3 = new User(momofin, "Momofin Admin Alex", "Alex", "alex@outlook.com", "Alex&Password0959", "Admin", true, true);

        momofinUsers = new ArrayList<User>();
        momofinUsers.add(user1);
        momofinUsers.add(user2);
        momofinUsers.add(user3);

        otherOrganization = new Organization("Dondozo");
        User user4 = new User(otherOrganization, "Dondozo Intern Ron", "Ron", "temp-intern@yahoo.com", "123456", "Intern");
        User user5 = new User(otherOrganization, "Dondozo Commander Tatsugiri","Tatsugiri", "commander@email.com", "toxic%Mouth", "Commander", true);

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
        String password = userToAuthenticate.getPassword();
        String organizationName = otherOrganization.getName();

        when(organizationRepository.findOrganizationByName(organizationName)).thenReturn(Optional.of(otherOrganization));
        when(userRepository.findUserByOrganizationAndUsernameAndPassword(
                otherOrganization,
                username,
                password
        )).thenReturn(Optional.of(userToAuthenticate));

        User authenticatedUser = userService.authenticate(
                organizationName,
                username,
                password);

        assertEquals(userToAuthenticate, authenticatedUser);
        assertEquals(username, authenticatedUser.getUsername());
        assertEquals(password, authenticatedUser.getPassword());

        verify(organizationRepository, times(1))
                .findOrganizationByName(organizationName);
        verify(userRepository, times(1))
                .findUserByOrganizationAndUsernameAndPassword(otherOrganization, username, password);
    }

    @Test
    void testAuthenticateInvalidOrganizationName() {
        String organizationName = "invalid";
        when(organizationRepository.findOrganizationByName(organizationName)).thenReturn(Optional.empty());


        assertThrows(OrganizationNotFoundException.class,
                () -> userService.authenticate(organizationName, "email", "password"));

        verify(organizationRepository, times(1))
                .findOrganizationByName(organizationName);
        verify(userRepository, never()).findUserByOrganizationAndUsernameAndPassword(any(Organization.class), anyString(), anyString());
    }

    @Test
    void testAuthenticateIncorrectUsername() {
        User userToAuthenticate = otherOrganizationUsers.getFirst();
        String username = "Wrong Username";
        String password = userToAuthenticate.getPassword();
        String organizationName = otherOrganization.getName();

        when(organizationRepository.findOrganizationByName(organizationName)).thenReturn(Optional.of(otherOrganization));
        when(userRepository.findUserByOrganizationAndUsernameAndPassword(
                otherOrganization,
                username,
                password
        )).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> userService.authenticate(organizationName, username, password));

        verify(organizationRepository, times(1))
                .findOrganizationByName(organizationName);
        verify(userRepository, times(1))
                .findUserByOrganizationAndUsernameAndPassword(otherOrganization, username, password);
    }

    @Test
    void testAuthenticateIncorrectPassword() {
        User userToAuthenticate = otherOrganizationUsers.getFirst();
        String username = userToAuthenticate.getUsername();
        String password = "Wrong Password";
        String organizationName = otherOrganization.getName();

        when(organizationRepository.findOrganizationByName(organizationName)).thenReturn(Optional.of(otherOrganization));
        when(userRepository.findUserByOrganizationAndUsernameAndPassword(
                otherOrganization,
                username,
                password
        )).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> userService.authenticate(organizationName, username, password));

        verify(organizationRepository, times(1))
                .findOrganizationByName(organizationName);
        verify(userRepository, times(1))
                .findUserByOrganizationAndUsernameAndPassword(otherOrganization, username, password);
    }

    @Test
    void testRegisterMember() {
        User userToBeRegistered = momofinUsers.getFirst();
        String username = userToBeRegistered.getUsername();
        String name = userToBeRegistered.getName();
        String email = userToBeRegistered.getEmail();
        String password = userToBeRegistered.getPassword();
        String position = userToBeRegistered.getPosition();

        when(userRepository.findUserByUsernameOrEmail(username, email)).thenReturn(new ArrayList<User>());
        when(userRepository.save(any(User.class))).thenReturn(userToBeRegistered);

        User registeredUser = userService.registerMember(momofin, username, name, email, password, position);

        assertEquals(userToBeRegistered, registeredUser);
        verify(userRepository, times(1)).findUserByUsernameOrEmail(username, email);
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

        verify(userRepository, never()).findUserByUsernameOrEmail(anyString(), anyString());
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

        List<User> fetchResult = new ArrayList<>();
        fetchResult.add(existingUser);

        when(userRepository.findUserByUsernameOrEmail(username, email)).thenReturn(fetchResult);

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class,
                () -> userService.registerMember(momofin, username, name, email, password, position));

        verify(userRepository, times(1)).findUserByUsernameOrEmail(username, email);
        verify(userRepository, never()).save(any(User.class));
        assertEquals("The username "+username+" is already in use", exception.getMessage());
    }

    @Test
    void testRegisterMemberEmailInUse() {
        User userToBeRegistered = momofinUsers.getFirst();
        String username = userToBeRegistered.getUsername();
        String name = userToBeRegistered.getName();
        String email = userToBeRegistered.getEmail();
        String password = userToBeRegistered.getPassword();
        String position = userToBeRegistered.getPosition();

        User existingUser = new User();
        existingUser.setEmail(email);

        List<User> fetchResult = new ArrayList<>();
        fetchResult.add(existingUser);

        when(userRepository.findUserByUsernameOrEmail(username, email)).thenReturn(fetchResult);

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class,
                () -> userService.registerMember(momofin, username, name, email, password, position));

        verify(userRepository, times(1)).findUserByUsernameOrEmail(username, email);
        verify(userRepository, never()).save(any(User.class));
        assertEquals("The email "+email+" is already in use", exception.getMessage());
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
}
