package ppl.momofin.momofinbackend.controller;

import io.sentry.Sentry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ppl.momofin.momofinbackend.error.InvalidOrganizationException;
import ppl.momofin.momofinbackend.error.OrganizationNotFoundException;
import ppl.momofin.momofinbackend.error.SecurityValidationException;
import ppl.momofin.momofinbackend.error.UserAlreadyExistsException;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.repository.OrganizationRepository;
import ppl.momofin.momofinbackend.response.FetchAllUserResponse;
import ppl.momofin.momofinbackend.service.OrganizationService;
import ppl.momofin.momofinbackend.response.OrganizationResponse;
import ppl.momofin.momofinbackend.service.UserService;
import ppl.momofin.momofinbackend.request.AddOrganizationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/momofin-admin")
public class MomofinAdminController {
    private static final Logger logger = LoggerFactory.getLogger(MomofinAdminController.class);

    private final OrganizationService organizationService;
    private final UserService userService;
    private final OrganizationRepository organizationRepository;

    @Autowired
    public MomofinAdminController(OrganizationService organizationService,
                                  UserService userService,
                                  OrganizationRepository organizationRepository) {
        this.organizationService = organizationService;
        this.userService = userService;
        this.organizationRepository = organizationRepository;
    }

    @GetMapping("/organizations")
    public ResponseEntity<List<OrganizationResponse>> getAllOrganizations() {
        List<Organization> organizations = organizationService.getAllOrganizations();
        List<OrganizationResponse> responses = organizations.stream()
                .map(OrganizationResponse::fromOrganization)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/users")
    public ResponseEntity<List<FetchAllUserResponse>> getAllUsers() {
        List<User> users = userService.fetchAllUsers();
        List<FetchAllUserResponse> responses = users.stream()
                .map(FetchAllUserResponse::fromUser)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/organizations")
    @Transactional
    public ResponseEntity<OrganizationResponse> addOrganization(@RequestBody AddOrganizationRequest request) {
        try {
            Organization newOrganization = createOrganization(request);
            createOrganizationWithAdmin(newOrganization, request);

            // Success logging
            Sentry.captureMessage(String.format(
                    "[Success] Organization created - Name: %s, Industry: %s, ID: %s",
                    request.getName(),
                    request.getIndustry(),
                    newOrganization.getOrganizationId()
            ));

            return ResponseEntity.ok(OrganizationResponse.fromOrganization(newOrganization));
        } catch (SecurityValidationException | InvalidOrganizationException | UserAlreadyExistsException e) {
            // Handle all validation-related exceptions with 400
            logger.warn("Validation failed: {}", e.getMessage());
            Sentry.captureException(e);
            return ResponseEntity.badRequest().body(
                    new OrganizationResponse(null, e.getMessage(), request.getDescription())
            );
        } catch (Exception e) {
            // Handle unexpected errors with 500
            logger.error("Unexpected error creating organization", e);
            Sentry.captureException(e);
            return ResponseEntity.internalServerError().body(
                    new OrganizationResponse(null, "An unexpected error occurred: " + e.getMessage(), request.getDescription())
            );
        }
    }

    private void createOrganizationWithAdmin(Organization organization, AddOrganizationRequest request) {
        try {
            createOrganizationAdmin(organization, request);
        } catch (Exception e) {
            organizationRepository.delete(organization);
            throw e;
        }
    }

    private Organization createOrganization(AddOrganizationRequest request) {
        return organizationService.createOrganization(
                request.getName(),
                request.getDescription(),
                request.getIndustry(),
                request.getLocation()
        );
    }

    private void createOrganizationAdmin(Organization organization, AddOrganizationRequest request) {
        userService.registerOrganizationAdmin(
                organization,
                request.getAdminUsername(),
                request.getName() + " Admin",
                null,
                request.getAdminPassword(),
                null
        );
    }


}
