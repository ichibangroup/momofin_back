package ppl.momofin.momofinbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ppl.momofin.momofinbackend.model.AuditTrail;
import ppl.momofin.momofinbackend.response.AuditTrailResponse;
import ppl.momofin.momofinbackend.service.AuditTrailService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/audit")
public class AuditTrailController {

    private final AuditTrailService auditTrailService;

    @Autowired
    public AuditTrailController(AuditTrailService auditTrailService) {
        this.auditTrailService = auditTrailService;
    }

    @GetMapping("/audits")
    public ResponseEntity<Page<AuditTrailResponse>> getAllAudits(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String documentName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        // Map sortBy to handle foreign key fields
        String resolvedSortBy = resolveSortField(sortBy);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), resolvedSortBy));

        LocalDateTime startDateTime = (startDate != null && !startDate.isEmpty())
                ? LocalDateTime.parse(startDate)
                : null;

        LocalDateTime endDateTime = (endDate != null && !endDate.isEmpty())
                ? LocalDateTime.parse(endDate)
                : null;

        Page<AuditTrail> auditTrailPage = auditTrailService.getAuditTrails(username, action, startDateTime, endDateTime, documentName, pageable);

        Page<AuditTrailResponse> responsePage = auditTrailPage.map(AuditTrailResponse::fromAuditTrail);

        return ResponseEntity.ok(responsePage);
    }

    // Helper method to map sortBy fields to database column names
    private String resolveSortField(String sortBy) {
        return switch (sortBy) {
            case "username" -> "user.username"; // Assuming the User entity is joined with the field `user`
            case "documentName" -> "document.name"; // Assuming the Document entity is joined with the field `document`
            case "action" -> "action";
            default -> "timestamp";
        };
    }



    @GetMapping("/{id}")
    public ResponseEntity<AuditTrail> getAuditTrailById(@PathVariable Long id) {
        AuditTrail auditTrail = auditTrailService.getAuditTrailById(id);
        return ResponseEntity.ok(auditTrail);
    }

    @PostMapping
    public ResponseEntity<AuditTrail> createAuditTrail(@RequestBody AuditTrail auditTrail) {
        AuditTrail createdAuditTrail = auditTrailService.createAuditTrail(auditTrail);
        return ResponseEntity.status(201).body(createdAuditTrail);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuditTrail(@PathVariable Long id) {
        auditTrailService.deleteAuditTrail(id);
        return ResponseEntity.noContent().build();
    }
}
