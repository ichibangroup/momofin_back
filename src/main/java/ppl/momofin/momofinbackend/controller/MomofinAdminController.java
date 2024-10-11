package ppl.momofin.momofinbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ppl.momofin.momofinbackend.error.InvalidOrganizationException;
import ppl.momofin.momofinbackend.error.OrganizationNotFoundException;
import ppl.momofin.momofinbackend.error.UserAlreadyExistsException;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.response.ErrorResponse;
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
        try {
            Organization newOrganization = createOrganization(request);
            createOrganizationAdmin(newOrganization, request);
            return ResponseEntity.ok(OrganizationResponse.fromOrganization(newOrganization));
        } catch (InvalidOrganizationException | UserAlreadyExistsException e) {
            return ResponseEntity.badRequest().body(new OrganizationResponse(null, e.getMessage(), request.getDescription()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new OrganizationResponse(null, "An unexpected error occurred", request.getDescription()));
        }
    }

    private Organization createOrganization(AddOrganizationRequest request) {
        return organizationService.createOrganization(request.getName(), request.getDescription());
    }

    private void createOrganizationAdmin(Organization organization, AddOrganizationRequest request) {
        userService.registerOrganizationAdmin(
                organization,
                request.getAdminUsername(),
                organization.getName() + " Admin",
                null,
                request.getAdminPassword(),
                null
        );
    }
    @PutMapping("/organizations/{orgId}")
    public ResponseEntity<OrganizationResponse> updateOrganization(@PathVariable Long orgId, @RequestBody AddOrganizationRequest request) {
        try {
            Organization updatedOrganization = organizationService.updateOrganization(orgId, request.getName(), request.getDescription());
            return ResponseEntity.ok(OrganizationResponse.fromOrganization(updatedOrganization));
        } catch (OrganizationNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (InvalidOrganizationException e) {
            return ResponseEntity.badRequest().body(new OrganizationResponse(null, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new OrganizationResponse(null, "An unexpected error occurred", null));
        }
    }


}