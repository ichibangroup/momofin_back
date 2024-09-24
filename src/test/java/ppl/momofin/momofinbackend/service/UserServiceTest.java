package ppl.momofin.momofinbackend.service;

import error.InvalidCredentialsException;
import error.InvalidPasswordException;
import error.OrganizationNotFoundException;
import error.UserAlreadyExistsException;
import model.Organization;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import repository.OrganizationRepository;
import repository.UserRepository;
import service.UserServiceImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        User user1 = new User(momofin,"Samuel", "samuel@gmail.com", "My#Money9078", "Finance Manager");
        User user2 = new User(momofin, "Darrel Hoei", "darellhoei@gmail.com", "HisPassword#6768", "Co-Founder", true);
        User user3 = new User(momofin, "Alex", "alex@outlook.com", "Alex&Password0959", "Admin", true, true);

        momofinUsers.add(user1);
        momofinUsers.add(user2);
        momofinUsers.add(user3);

        otherOrganization = new Organization("Dondozo");
        User user4 = new User(otherOrganization, "Intern, yes that is his name", "temp-intern@yahoo.com", "123456", "Intern");
        User user5 = new User(otherOrganization, "Tatsugiri", "commander@email.com", "toxic%Mouth", "Commander", true);

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
        String email = userToAuthenticate.getEmail();
        String password = userToAuthenticate.getPassword();
        String organizationName = otherOrganization.getName();

        when(organizationRepository.findOrganizationByName(organizationName)).thenReturn(Optional.of(otherOrganization));
        when(userRepository.findUserByOrganizationAndNameAndPassword(
                otherOrganization,
                email,
                password
        )).thenReturn(Optional.of(userToAuthenticate));

        User authenticatedUser = userService.authenticate(
                organizationName,
                email,
                password);

        assertEquals(userToAuthenticate, authenticatedUser);
        assertEquals(email, authenticatedUser.getEmail());
        assertEquals(password, authenticatedUser.getEmail());

        verify(organizationRepository, times(1))
                .findOrganizationByName(organizationName);
        verify(userRepository, times(1))
                .findUserByOrganizationAndNameAndPassword(otherOrganization, email, password);
    }

    @Test
    void testAuthenticateInvalidOrganizationName() {
        String organizationName = "invalid";
        when(organizationRepository.findOrganizationByName(organizationName)).thenReturn(Optional.empty());


        assertThrows(OrganizationNotFoundException.class,
                () -> userService.authenticate(organizationName, "email", "password"));

        verify(organizationRepository, times(1))
                .findOrganizationByName(organizationName);
        verify(userRepository, never()).findUserByOrganizationAndNameAndPassword(any(Organization.class), anyString(), anyString());
    }

    @Test
    void testAuthenticateIncorrectEmail() {
        User userToAuthenticate = otherOrganizationUsers.getFirst();
        String email = "Wrong Email";
        String password = userToAuthenticate.getPassword();
        String organizationName = otherOrganization.getName();

        when(organizationRepository.findOrganizationByName(organizationName)).thenReturn(Optional.of(otherOrganization));
        when(userRepository.findUserByOrganizationAndNameAndPassword(
                otherOrganization,
                email,
                password
        )).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> userService.authenticate(organizationName, email, password));

        verify(organizationRepository, times(1))
                .findOrganizationByName(organizationName);
        verify(userRepository, times(1))
                .findUserByOrganizationAndNameAndPassword(otherOrganization, email, password);
    }

    @Test
    void testAuthenticateIncorrectPassword() {
        User userToAuthenticate = otherOrganizationUsers.getFirst();
        String email = userToAuthenticate.getEmail();
        String password = "Wrong Password";
        String organizationName = otherOrganization.getName();

        when(organizationRepository.findOrganizationByName(organizationName)).thenReturn(Optional.of(otherOrganization));
        when(userRepository.findUserByOrganizationAndNameAndPassword(
                otherOrganization,
                email,
                password
        )).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> userService.authenticate(organizationName, email, password));

        verify(organizationRepository, times(1))
                .findOrganizationByName(organizationName);
        verify(userRepository, times(1))
                .findUserByOrganizationAndNameAndPassword(otherOrganization, email, password);
    }

    @Test
    void testRegisterMember() {
        User userToBeRegistered = momofinUsers.getFirst();
        String name = userToBeRegistered.getName();
        String email = userToBeRegistered.getEmail();
        String password = userToBeRegistered.getPassword();
        String position = userToBeRegistered.getPosition();

        when(userRepository.findByOrganizationAndNameOrEmail(momofin, name, email)).thenReturn(new ArrayList<User>());
        when(userRepository.save(userToBeRegistered)).thenReturn(userToBeRegistered);

        User registeredUser = userService.registerMember(momofin, name, email, password, position);

        assertEquals(userToBeRegistered, registeredUser);
        verify(userRepository, times(1)).findByOrganizationAndNameOrEmail(momofin, name, email);
        verify(userRepository, times(1)).save(userToBeRegistered);
    }

    @Test
    void testRegisterMemberPasswordTooShort() {
        User userToBeRegistered = momofinUsers.getFirst();
        String name = userToBeRegistered.getName();
        String email = userToBeRegistered.getEmail();
        String password = "Thisistoo";
        String position = userToBeRegistered.getPosition();

        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class,
                () -> userService.registerMember(momofin, name, email, password, position));

        verify(userRepository, never()).findByOrganizationAndNameOrEmail(any(Organization.class), anyString(), anyString());
        verify(userRepository, never()).save(any(User.class));

        assertEquals("Password must be at least 10 characters long.", exception.getMessage());
    }

    @Test
    void testRegisterMemberAlreadyExists() {
        User userToBeRegistered = momofinUsers.getFirst();
        String name = userToBeRegistered.getName();
        String email = userToBeRegistered.getEmail();
        String password = userToBeRegistered.getPassword();
        String position = userToBeRegistered.getPosition();

        List<User> fetchResult = new ArrayList<>();
        fetchResult.add(userToBeRegistered);

        when(userRepository.findByOrganizationAndNameOrEmail(momofin, name, email)).thenReturn(fetchResult);

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.registerMember(momofin, name, email, password, position));

        verify(userRepository, times(1)).findByOrganizationAndNameOrEmail(momofin, name, email);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testFetchAllOrganizationalUsers() {
        when(userRepository.findAllByOrganization(momofin)).thenReturn(momofinUsers);

        List<User> fetchedUsers = userService.fetchUsersByOrganization(momofin);

        assertEquals(fetchedUsers, momofinUsers);
        verify(userRepository, times(1)).findAllByOrganization(momofin);
    }

    @Test
    void testFetchAllUsers() {
        momofinUsers.addAll(otherOrganizationUsers);
        when(userRepository.findAll()).thenReturn(momofinUsers);

        List<User> fetchedUsers = userService.fetchAllUsers();

        assertEquals(fetchedUsers, momofinUsers);
        verify(userRepository, times(1)).findAll();
    }
}
