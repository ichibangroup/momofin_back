package ppl.momofin.momofinbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ppl.momofin.momofinbackend.dto.UserDTO;
import ppl.momofin.momofinbackend.model.Organization;
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
    public ResponseEntity<List<UserDTO>> getUsersInOrganization(@PathVariable Long orgId) {
        List<UserDTO> users = organizationService.getUsersInOrganization(orgId);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/{orgId}/users")
    public ResponseEntity<UserDTO> addUserToOrganization(@PathVariable Long orgId, @RequestBody UserDTO userDTO) {
        UserDTO addedUser = organizationService.addUserToOrganization(orgId, userDTO);
        return ResponseEntity.ok(addedUser);
    }

    @DeleteMapping("/{orgId}/users/{userId}")
    public ResponseEntity<Void> removeUserFromOrganization(@PathVariable Long orgId, @PathVariable Long userId) {
        organizationService.removeUserFromOrganization(orgId, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{orgId}/users/{userId}")
    public ResponseEntity<UserDTO> updateUserInOrganization(@PathVariable Long orgId, @PathVariable Long userId, @RequestBody UserDTO userDTO) {
        UserDTO updatedUser = organizationService.updateUserInOrganization(orgId, userId, userDTO);
        return ResponseEntity.ok(updatedUser);
    }
}