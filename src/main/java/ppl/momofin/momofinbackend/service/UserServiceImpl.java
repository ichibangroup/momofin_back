package ppl.momofin.momofinbackend.service;


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

    @Override
    public User authenticate(String organizationName, String email, String password) {
        Optional<Organization> organization = organizationRepository.findOrganizationByName(organizationName);

        if(organization.isEmpty()) {
            throw new OrganizationNotFoundException(organizationName);
        }

        Optional<User> user = userRepository.findUserByOrganizationAndEmailAndPassword(organization.get(), email, password);

        if(user.isEmpty()) {
            throw new InvalidCredentialsException();
        }

        return user.get();
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User registerMember(Organization organization, String name, String email, String password, String position) {
        PasswordValidator.validatePassword(password);

        List<User> fetchResults = userRepository.findByOrganizationAndNameOrEmail(organization, name, email);

        if (!fetchResults.isEmpty()) {
            User existingUser = fetchResults.getFirst();

            if (email.equals(existingUser.getEmail())) {
                throw new UserAlreadyExistsException("The email "+email+" is already in use");
            } else if (name.equals(existingUser.getName())) {
                throw new UserAlreadyExistsException("You have already registered a "+ name +" to the organization");
            }
        }

        return userRepository.save(new User(organization, name, email, password, position));
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
