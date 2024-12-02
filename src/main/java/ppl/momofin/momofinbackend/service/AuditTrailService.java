package ppl.momofin.momofinbackend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ppl.momofin.momofinbackend.model.AuditTrail;
import ppl.momofin.momofinbackend.model.Organization;

import java.time.LocalDateTime;

public interface AuditTrailService {
    Page<AuditTrail> getAuditTrails(Organization organization, String username, String action, LocalDateTime startDateTime, LocalDateTime endDateTime, String documentName, Pageable pageable);
    Integer getTotalCount(Organization organization, String username, String action, LocalDateTime startDateTime, LocalDateTime endDateTime, String documentName);
    void createAuditTrail(AuditTrail auditTrail);
}