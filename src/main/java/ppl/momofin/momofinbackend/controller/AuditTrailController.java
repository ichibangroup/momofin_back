package ppl.momofin.momofinbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ppl.momofin.momofinbackend.model.AuditTrail;
import ppl.momofin.momofinbackend.response.AuditTrailResponse;
import ppl.momofin.momofinbackend.service.AuditTrailService;

import java.util.List;

@RestController
@RequestMapping("/audit")
public class AuditTrailController {

    private final AuditTrailService auditTrailService;

    @Autowired
    public AuditTrailController(AuditTrailService auditTrailService) {
        this.auditTrailService = auditTrailService;
    }

    @GetMapping("/audits")
    public ResponseEntity<List<AuditTrailResponse>> getAllAudits() {
        List<AuditTrail> audits = auditTrailService.getAllAuditTrails();
        List<AuditTrailResponse> responses = audits.stream()
                .map(AuditTrailResponse::fromAuditTrail)
                .toList();
        return ResponseEntity.ok(responses);
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
