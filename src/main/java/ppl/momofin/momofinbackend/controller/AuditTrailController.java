package ppl.momofin.momofinbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ppl.momofin.momofinbackend.model.AuditTrail;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.response.AuditTrailResponse;
import ppl.momofin.momofinbackend.service.AuditTrailService;
import ppl.momofin.momofinbackend.service.DocumentService;
import ppl.momofin.momofinbackend.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/audit")
public class AuditTrailController {

    private final AuditTrailService auditTrailService;
    private final UserService userService;
    private final DocumentService documentService;

    @Autowired
    public AuditTrailController(AuditTrailService auditTrailService, UserService userService, DocumentService documentService) {
        this.auditTrailService = auditTrailService;
        this.userService = userService;
        this.documentService = documentService;
    }

    @GetMapping("/audits")
    public ResponseEntity<Page<AuditTrailResponse>> getAllAudits(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));

        User user = null;
        if (username != null && !username.isEmpty()) {
            user = userService.fetchUserByUsername(username);
        }

        LocalDateTime startDateTime = startDate != null ? LocalDateTime.parse(startDate) : null;
        LocalDateTime endDateTime = endDate != null ? LocalDateTime.parse(endDate) : null;

        Page<AuditTrail> auditTrailPage = auditTrailService.getAuditTrails(user, action, startDateTime, endDateTime, pageable);

        Page<AuditTrailResponse> responsePage = auditTrailPage.map(AuditTrailResponse::fromAuditTrail);

        return ResponseEntity.ok(responsePage);
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
