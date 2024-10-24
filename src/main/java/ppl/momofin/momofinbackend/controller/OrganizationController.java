package ppl.momofin.momofinbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ppl.momofin.momofinbackend.dto.UserDTO;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.service.OrganizationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    @Autowired
    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping("/{orgId}")
    public ResponseEntity<Organization> getOrganization(@PathVariable String orgId) {
        Organization organization = organizationService.findOrganizationById(UUID.fromString(orgId));
        return ResponseEntity.ok(organization);
    }

    @PutMapping("/{orgId}")
    public ResponseEntity<Organization> updateOrganization(@PathVariable String orgId, @RequestBody Organization organizationDetails) {
        Organization updatedOrganization = organizationService.updateOrganization(UUID.fromString(orgId), organizationDetails.getName(), organizationDetails.getDescription(), organizationDetails.getIndustry(), organizationDetails.getLocation());
        return ResponseEntity.ok(updatedOrganization);
    }

    @GetMapping("/{orgId}/users")
    public ResponseEntity<List<UserDTO>> getUsersInOrganization(@PathVariable String orgId) {
        List<UserDTO> users = organizationService.getUsersInOrganization(UUID.fromString(orgId));
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{orgId}/users/{userId}")
    public ResponseEntity<Void> removeUserFromOrganization(@PathVariable String orgId, @PathVariable String userId) {
        organizationService.removeUserFromOrganization(UUID.fromString(orgId), UUID.fromString(userId));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{orgId}/users/{userId}")
    public ResponseEntity<UserDTO> updateUserInOrganization(@PathVariable String orgId, @PathVariable String userId, @RequestBody UserDTO userDTO) {
        UserDTO updatedUser = organizationService.updateUserInOrganization(UUID.fromString(orgId), UUID.fromString(userId), userDTO);
        return ResponseEntity.ok(updatedUser);
    }
}