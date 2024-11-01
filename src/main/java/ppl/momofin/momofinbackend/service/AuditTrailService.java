package ppl.momofin.momofinbackend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ppl.momofin.momofinbackend.model.AuditTrail;
import ppl.momofin.momofinbackend.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditTrailService {
    List<AuditTrail> getAllAuditTrails();
    Page<AuditTrail> getAuditTrails(User user, String action, LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable);
    AuditTrail getAuditTrailById(Long id);
    AuditTrail createAuditTrail(AuditTrail auditTrail);
    void deleteAuditTrail(Long id);
}
