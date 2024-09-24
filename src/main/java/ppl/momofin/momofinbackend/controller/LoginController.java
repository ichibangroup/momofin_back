package ppl.momofin.momofinbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.service.UserService;

import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public ModelAndView loginPage() {
            return new ModelAndView("login");
    }

    @PostMapping("/login")
    public ModelAndView login(@RequestParam String identifier, @RequestParam String password) {
        Optional<User> userOptional = userService.findByEmailOrCompanyName(identifier);

        if (userOptional.isPresent() && userService.validatePassword(userOptional.get(), password)) {
            return new ModelAndView("redirect:/");
        } else {
            ModelAndView modelAndView = new ModelAndView("login");
            modelAndView.addObject("error", "Invalid Credentials");
            return modelAndView;
        }
    }
}
