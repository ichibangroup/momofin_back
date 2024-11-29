package ppl.momofin.momofinbackend.controller;

import io.sentry.Sentry;
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
        String resolvedSortBy = resolveSortField(sortBy);

        Sentry.captureMessage("Fetching audit trails with parameters - username: " + username + ", action: " + action +
                ", startDate: " + startDate + ", endDate: " + endDate + ", documentName: " + documentName +
                ", page: " + page + ", size: " + size + ", sortBy: " + sortBy + ", direction: " + direction);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), resolvedSortBy));

        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        try {
            if (startDate != null && !startDate.isEmpty()) {
                startDateTime = LocalDateTime.parse(startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                endDateTime = LocalDateTime.parse(endDate);
            }
        } catch (Exception e) {
            Sentry.captureException(e);
            return ResponseEntity.badRequest().body(null);
        }

        try {
            Page<AuditTrail> auditTrailPage = auditTrailService.getAuditTrails(username, action, startDateTime, endDateTime, documentName, pageable);
            Page<AuditTrailResponse> responsePage = auditTrailPage.map(AuditTrailResponse::fromAuditTrail);
            return ResponseEntity.ok(responsePage);
        } catch (Exception e) {
            Sentry.captureException(e);
            return ResponseEntity.status(500).body(null);
        }
    }

    private String resolveSortField(String sortBy) {
        return switch (sortBy) {
            case "username" -> "user.username";
            case "documentName" -> "document.name";
            case "action" -> "action";
            default -> "timestamp";
        };
    }
}