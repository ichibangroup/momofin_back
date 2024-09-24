package ppl.momofin.momofinbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.repository.UserRepository;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Optional<User> findByEmailOrCompanyName(String identifier) {
        Optional<User> user = userRepository.findByEmail(identifier);

        if (user.isEmpty()) {
            user = userRepository.findByCompanyName(identifier);
        }

        return user;
    }

    public boolean validatePassword(User user, String password) {
        return user.getPassword().equals(password);
    }
}
