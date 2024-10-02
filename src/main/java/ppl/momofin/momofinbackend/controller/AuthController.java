package ppl.momofin.momofinbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.request.RegisterRequest;
import ppl.momofin.momofinbackend.response.Response;
import ppl.momofin.momofinbackend.response.ErrorResponse;
import ppl.momofin.momofinbackend.response.AuthResponseSuccess;
import ppl.momofin.momofinbackend.response.RegisterResponseSuccess;
import ppl.momofin.momofinbackend.service.UserService;
import ppl.momofin.momofinbackend.request.AuthRequest;
import ppl.momofin.momofinbackend.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ppl.momofin.momofinbackend.controller.DocumentVerificationController.getUsername;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<Response> authenticateUser(@RequestBody AuthRequest authRequest) {
        try {
            User authenticatedUser = userService.authenticate(
                    authRequest.getOrganizationName(),
                    authRequest.getUsername(),
                    authRequest.getPassword()
            );
            String jwt = jwtUtil.generateToken(authenticatedUser.getUsername());

            logger.info("Successful login for user: {} from organization: {}",
                    authRequest.getUsername(), authRequest.getOrganizationName());

            AuthResponseSuccess response = new AuthResponseSuccess(authenticatedUser, jwt);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.warn("Failed login attempt for user: {} from organization: {}",
                    authRequest.getUsername(), authRequest.getOrganizationName());
            ErrorResponse response = new ErrorResponse(e.getMessage());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Response> registerUser(@RequestHeader("Authorization") String token, @RequestBody RegisterRequest registerRequest) {
        String username = authenticateAndGetUsername(token);
        User user = userService.fetchUserByUsername(username);
        try {
            User registeredUser = userService.registerMember(
                    user.getOrganization(),
                    registerRequest.getUsername(),
                    registerRequest.getName(),
                    registerRequest.getEmail(),
                    registerRequest.getPassword(),
                    registerRequest.getPosition()
            );

            logger.info("Successful register for user: {} from organization: {}",
                    registerRequest.getUsername(), user.getOrganization().getName());

            RegisterResponseSuccess response = new RegisterResponseSuccess(registeredUser);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ErrorResponse response = new ErrorResponse(e.getMessage());

            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }

    @GetMapping("/info")
    public ResponseEntity<User> getAuthenticatedUser(@RequestHeader("Authorization") String token) {
        String username = authenticateAndGetUsername(token);

        User user = userService.fetchUserByUsername(username);

        return ResponseEntity.ok(user);
    }

    private String authenticateAndGetUsername(String token) {
        return getUsername(token, jwtUtil);
    }
}
