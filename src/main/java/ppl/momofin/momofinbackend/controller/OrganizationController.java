package ppl.momofin.momofinbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.service.OrganizationService;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    @PutMapping("/{orgId}")
    public ResponseEntity<Organization> updateOrganization(@PathVariable Long orgId, @RequestBody Organization organizationDetails) {
        Organization updatedOrganization = organizationService.updateOrganization(orgId, organizationDetails.getName(), organizationDetails.getDescription());
        return ResponseEntity.ok(updatedOrganization);
    }

    @GetMapping("/{orgId}/users")
    public ResponseEntity<List<User>> getUsersInOrganization(@PathVariable Long orgId) {
        List<User> users = organizationService.getUsersInOrganization(orgId);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/{orgId}/users")
    public ResponseEntity<User> addUserToOrganization(@PathVariable Long orgId, @RequestBody User user) {
        User addedUser = organizationService.addUserToOrganization(orgId, user);
        return ResponseEntity.ok(addedUser);
    }

    @DeleteMapping("/{orgId}/users/{userId}")
    public ResponseEntity<Void> removeUserFromOrganization(@PathVariable Long orgId, @PathVariable Long userId) {
        organizationService.removeUserFromOrganization(orgId, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{orgId}/users/{userId}")
    public ResponseEntity<User> updateUserInOrganization(@PathVariable Long orgId, @PathVariable Long userId, @RequestBody User userDetails) {
        User updatedUser = organizationService.updateUserInOrganization(orgId, userId, userDetails);
        return ResponseEntity.ok(updatedUser);
    }
}