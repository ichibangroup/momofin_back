package ppl.momofin.momofinbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ppl.momofin.momofinbackend.dto.UserDTO;
import ppl.momofin.momofinbackend.error.OrganizationNotFoundException;
import ppl.momofin.momofinbackend.error.UserDeletionException;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.response.ErrorResponse;
import ppl.momofin.momofinbackend.response.Response;
import ppl.momofin.momofinbackend.security.JwtUtil;
import ppl.momofin.momofinbackend.service.OrganizationService;
import ppl.momofin.momofinbackend.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {
    private final OrganizationService organizationService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public OrganizationController(OrganizationService organizationService,
                                  UserService userService,
                                  JwtUtil jwtUtil) {
        this.organizationService = organizationService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/{orgId}")
    public ResponseEntity<Organization> getOrganization(@PathVariable Long orgId) {
        Organization organization = organizationService.findOrganizationById(orgId);
        return ResponseEntity.ok(organization);
    }

    @PutMapping("/{orgId}")
    public ResponseEntity<Organization> updateOrganization(@PathVariable Long orgId, @RequestBody Organization organizationDetails) {
        Organization updatedOrganization = organizationService.updateOrganization(orgId, organizationDetails.getName(), organizationDetails.getDescription(), organizationDetails.getIndustry(), organizationDetails.getLocation());
        return ResponseEntity.ok(updatedOrganization);
    }

    @GetMapping("/{orgId}/users")
    public ResponseEntity<List<UserDTO>> getUsersInOrganization(@PathVariable Long orgId) {
        List<UserDTO> users = organizationService.getUsersInOrganization(orgId);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{orgId}/users/{userId}")
    public ResponseEntity<UserDTO> updateUserInOrganization(@PathVariable Long orgId, @PathVariable Long userId, @RequestBody UserDTO userDTO) {
        UserDTO updatedUser = organizationService.updateUserInOrganization(orgId, userId, userDTO);
        return ResponseEntity.ok(updatedUser);
    }
    @DeleteMapping("/{orgId}/users/{userId}")
    public ResponseEntity<Response> deleteUser(
            @PathVariable Long orgId,
            @PathVariable Long userId,
            @RequestHeader("Authorization") String token) {
        try {
            String username = getUsername(token);
            User requestingUser = userService.fetchUserByUsername(username);

            organizationService.deleteUser(orgId, userId, requestingUser);
            return ResponseEntity.noContent().build();
        } catch (UserDeletionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (OrganizationNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    private String getUsername(String token) {
        // Remove "Bearer " prefix and then use correct method name
        token = token.substring(7);
        return jwtUtil.extractUsername(token);
    }

}