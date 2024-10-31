package ppl.momofin.momofinbackend.service;

import org.springframework.data.domain.Page;
import ppl.momofin.momofinbackend.model.AuditTrail;

import java.util.List;

public interface AuditTrailService {
    List<AuditTrail> getAllAuditTrails();
    Page<AuditTrail> getAuditTrails(String action, String user, int page, int size, String sortBy, String direction);
    AuditTrail getAuditTrailById(Long id);
    AuditTrail createAuditTrail(AuditTrail auditTrail);
    void deleteAuditTrail(Long id);
}
