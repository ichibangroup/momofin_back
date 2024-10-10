package ppl.momofin.momofinbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.service.OrganizationService;
import ppl.momofin.momofinbackend.response.OrganizationResponse;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/momofin-admin")
public class MomofinAdminController {

    private final OrganizationService organizationService;

    @Autowired
    public MomofinAdminController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping("/organizations")
    public ResponseEntity<List<OrganizationResponse>> getAllOrganizations() {
        List<Organization> organizations = organizationService.getAllOrganizations();
        List<OrganizationResponse> responses = organizations.stream()
                .map(OrganizationResponse::fromOrganization)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}