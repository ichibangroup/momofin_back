package ppl.momofin.momofinbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ppl.momofin.momofinbackend.annotation.RequireOrganizationAdmin;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.service.OrganizationService;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    @RequireOrganizationAdmin
    @PutMapping("/{orgId}")
    public ResponseEntity<Organization> updateOrganization(@PathVariable Long orgId, @RequestBody Organization organizationDetails) {
        Organization updatedOrganization = organizationService.updateOrganization(
                orgId,
                organizationDetails.getName(),
                organizationDetails.getDescription()
        );
        return ResponseEntity.ok(updatedOrganization);
    }
}