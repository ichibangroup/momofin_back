package ppl.momofin.momofinbackend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ppl.momofin.momofinbackend.model.AuditTrail;

import java.time.LocalDateTime;

public interface AuditTrailService {
    Page<AuditTrail> getAuditTrails(String username, String action, LocalDateTime startDateTime, LocalDateTime endDateTime, String documentName, Pageable pageable);
    void createAuditTrail(AuditTrail auditTrail);
}