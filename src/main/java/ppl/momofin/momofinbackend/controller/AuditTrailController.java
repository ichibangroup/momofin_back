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

    private String resolveSortField(String sortBy) {
        return switch (sortBy) {
            case "username" -> "user.username";
            case "documentName" -> "document.name";
            case "action" -> "action";
            default -> "timestamp";
        };
    }
}
