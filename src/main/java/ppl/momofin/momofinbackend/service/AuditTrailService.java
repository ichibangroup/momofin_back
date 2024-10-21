package ppl.momofin.momofinbackend.service;

import ppl.momofin.momofinbackend.model.AuditTrail;

import java.util.List;

public interface AuditTrailService {
    List<AuditTrail> getAllAuditTrails();
    AuditTrail getAuditTrailById(Long id);
    AuditTrail createAuditTrail(AuditTrail auditTrail);
    void deleteAuditTrail(Long id);
}
