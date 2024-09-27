package ppl.momofin.momofinbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ppl.momofin.momofinbackend.error.InvalidCredentialsException;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.response.AuthResponse;
import ppl.momofin.momofinbackend.response.AuthResponseFailure;
import ppl.momofin.momofinbackend.response.AuthResponseSuccess;
import ppl.momofin.momofinbackend.service.UserService;
import ppl.momofin.momofinbackend.request.AuthRequest;
import ppl.momofin.momofinbackend.utility.JwtUtil;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@RequestBody AuthRequest authRequest) {
        try {
            User authenticatedUser = userService.authenticate(
                    authRequest.getOrganizationName(),
                    authRequest.getUsername(),
                    authRequest.getPassword()
            );
            String jwt = jwtUtil.generateToken(authenticatedUser.getUsername());

            AuthResponseSuccess response = new AuthResponseSuccess(authenticatedUser, jwt);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            AuthResponseFailure response = new AuthResponseFailure(e.getMessage());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
