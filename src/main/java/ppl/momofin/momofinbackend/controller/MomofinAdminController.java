package ppl.momofin.momofinbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.service.OrganizationService;
import ppl.momofin.momofinbackend.response.OrganizationResponse;
import ppl.momofin.momofinbackend.service.UserService;
import ppl.momofin.momofinbackend.request.AddOrganizationRequest;

import java.util.List;

@RestController
@RequestMapping("/api/momofin-admin")
public class MomofinAdminController {

    private final OrganizationService organizationService;
    private final UserService userService;

    @Autowired
    public MomofinAdminController(OrganizationService organizationService, UserService userService) {
        this.organizationService = organizationService;
        this.userService = userService;
    }

    @GetMapping("/organizations")
    public ResponseEntity<List<OrganizationResponse>> getAllOrganizations() {
        List<Organization> organizations = organizationService.getAllOrganizations();
        List<OrganizationResponse> responses = organizations.stream()
                .map(OrganizationResponse::fromOrganization)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/organizations")
    public ResponseEntity<OrganizationResponse> addOrganization(@RequestBody AddOrganizationRequest request) {
        Organization newOrganization = organizationService.createOrganization(request.getName(), request.getDescription());

        userService.registerOrganizationAdmin(
                newOrganization,
                request.getAdminUsername(),
                request.getName() + " Admin", // Default name
                null, // email is null
                request.getAdminPassword(),
                null // position is null
        );

        return ResponseEntity.ok(OrganizationResponse.fromOrganization(newOrganization));
    }


}