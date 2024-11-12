package ppl.momofin.momofinbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ppl.momofin.momofinbackend.model.AuditTrail;
import ppl.momofin.momofinbackend.repository.AuditTrailRepository;
import ppl.momofin.momofinbackend.repository.specification.AuditTrailSpecifications;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AuditTrailServiceImpl implements AuditTrailService {

    private final AuditTrailRepository auditTrailRepository;

    @Autowired
    public AuditTrailServiceImpl(AuditTrailRepository auditTrailRepository) {
        this.auditTrailRepository = auditTrailRepository;
    }

    public List<AuditTrail> getAllAuditTrails() {
        return auditTrailRepository.findAll();
    }

    public Page<AuditTrail> getAuditTrails(String username, String action, LocalDateTime startDateTime, LocalDateTime endDateTime, String documentName, Pageable pageable) {
        Specification<AuditTrail> spec = Specification.where(
                        username != null ? AuditTrailSpecifications.hasUser(username) : null)
                .and(action != null ? AuditTrailSpecifications.hasAction(action) : null)
                .and(startDateTime != null ? AuditTrailSpecifications.afterTimestamp(startDateTime) : null)
                .and(endDateTime != null ? AuditTrailSpecifications.beforeTimestamp(endDateTime) : null)
                .and(documentName != null ? AuditTrailSpecifications.hasDocumentName(documentName) : null);

        return auditTrailRepository.findAll(spec, pageable);
    }

    public AuditTrail getAuditTrailById(Long id) {
        Optional<AuditTrail> auditTrail = auditTrailRepository.findById(id);
        return auditTrail.orElseThrow(() -> new IllegalArgumentException("AuditTrail not found"));
    }

    public AuditTrail createAuditTrail(AuditTrail auditTrail) {
        return auditTrailRepository.save(auditTrail);
    }

    public void deleteAuditTrail(Long id) {
        auditTrailRepository.deleteById(id);
    }
}