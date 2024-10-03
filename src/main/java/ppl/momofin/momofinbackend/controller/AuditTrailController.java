package ppl.momofin.momofinbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ppl.momofin.momofinbackend.model.AuditTrail;
import ppl.momofin.momofinbackend.service.AuditTrailService;

import java.util.List;

@RestController
@RequestMapping("/api/audit-trails")
public class AuditTrailController {

    private final AuditTrailService auditTrailService;

    @Autowired
    public AuditTrailController(AuditTrailService auditTrailService) {
        this.auditTrailService = auditTrailService;
    }

    @GetMapping
    public ResponseEntity<List<AuditTrail>> getAllAuditTrails() {
        List<AuditTrail> auditTrails = auditTrailService.getAllAuditTrails();
        return ResponseEntity.ok(auditTrails);
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
