package ppl.momofin.momofinbackend.controller;

import io.sentry.Sentry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ppl.momofin.momofinbackend.dto.UserDTO;
import ppl.momofin.momofinbackend.error.*;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.repository.OrganizationRepository;
import ppl.momofin.momofinbackend.response.ErrorResponse;
import ppl.momofin.momofinbackend.response.FetchAllUserResponse;
import ppl.momofin.momofinbackend.service.OrganizationService;
import ppl.momofin.momofinbackend.response.OrganizationResponse;
import ppl.momofin.momofinbackend.service.UserService;
import ppl.momofin.momofinbackend.request.AddOrganizationRequest;
import ppl.momofin.momofinbackend.response.Response;
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

    @PutMapping("/organizations/{orgId}")
    public ResponseEntity<OrganizationResponse> updateOrganization(
            @PathVariable String orgId,
            @RequestBody AddOrganizationRequest request) {
        try {
            Organization updatedOrganization = organizationService.updateOrganization(
                    UUID.fromString(orgId),
                    request.getName(),
                    request.getDescription(),
                    request.getIndustry(),
                    request.getLocation()
            );

            Sentry.captureMessage(String.format(
                    "[Success] Organization updated - ID: %s, New Name: %s",
                    orgId,
                    request.getName()
            ));

            return ResponseEntity.ok(OrganizationResponse.fromOrganization(updatedOrganization));
        } catch (SecurityValidationException | InvalidOrganizationException e) {
            // Handle validation-related exceptions with 400
            Sentry.captureException(e);
            return ResponseEntity.badRequest().body(new OrganizationResponse(null, e.getMessage(), null));
        } catch (OrganizationNotFoundException e) {
            // Handle not found with 404
            Sentry.captureException(e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // Handle unexpected errors with 500
            Sentry.captureException(e);
            return ResponseEntity.internalServerError().body(new OrganizationResponse(null, "An unexpected error occurred", null));
        }
    }
    @DeleteMapping("/organizations/{orgId}")
    public ResponseEntity<OrganizationResponse> deleteOrganization(@PathVariable String orgId) {
        try {
            organizationService.deleteOrganization(UUID.fromString(orgId));

            // Success logging
            Sentry.captureMessage(String.format(
                    "[Success] Organization deleted - ID: %s",
                    orgId
            ));

            return ResponseEntity.noContent().build();
        } catch (OrganizationNotFoundException e) {
            Sentry.captureException(e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Unexpected error deleting organization", e);
            Sentry.captureException(e);
            return ResponseEntity.status(HttpStatus.GONE)
                    .body(new OrganizationResponse(null, "Error deleting organization: " + e.getMessage(), null));
        }
    }
    @PutMapping("/organizations/name/{orgName}/users/{userId}/set-admin")
    public ResponseEntity<Response> setOrganizationAdmin(
            @PathVariable String orgName,
            @PathVariable String userId) {
        try {
            // First find organization by name
            Organization org = organizationRepository.findByName(orgName)
                    .orElseThrow(() -> new OrganizationNotFoundException("Organization not found with name: " + orgName));

            // Use existing service method with found org ID
            organizationService.setOrganizationAdmin(
                    org.getOrganizationId(),
                    UUID.fromString(userId)
            );

            // Success logging
            Sentry.captureMessage(String.format(
                    "[Success] User set as organization admin - UserID: %s, Organization: %s",
                    userId,
                    orgName
            ));

            return ResponseEntity.noContent().build();
        } catch (OrganizationNotFoundException e) {
            // Handle not found with 404
            Sentry.captureException(e);
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            // Handle security/validation errors
            logger.error("Security validation error: {}", e.getMessage());
            Sentry.captureException(e);
            return ResponseEntity.status(HttpStatus.GONE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            // Handle unexpected errors
            logger.error("Unexpected error setting organization admin", e);
            Sentry.captureException(e);
            return ResponseEntity.status(HttpStatus.GONE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse("Error setting organization admin: " + e.getMessage()));
        }
    }
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Response> deleteUser(@PathVariable String userId) {
        try {
            // Get the requesting Momofin admin from security context
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User requestingUser = userService.fetchUserByUsername(username);

            // Using the same service method, but with null orgId since Momofin admin doesn't need it
            organizationService.deleteUser(null, UUID.fromString(userId), requestingUser);

            // Success logging
            Sentry.captureMessage(String.format(
                    "[Success] User deleted by Momofin admin - UserID: %s",
                    userId
            ));

            return ResponseEntity.noContent().build();
        } catch (UserDeletionException e) {
            logger.error("Error deleting user: {}", e.getMessage());
            Sentry.captureException(e);
            return ResponseEntity.status(HttpStatus.GONE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error deleting user", e);
            Sentry.captureException(e);
            return ResponseEntity.status(HttpStatus.GONE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse("Error deleting user: " + e.getMessage()));
        }
    }
}
