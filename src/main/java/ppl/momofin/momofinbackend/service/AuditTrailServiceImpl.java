package ppl.momofin.momofinbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ppl.momofin.momofinbackend.model.AuditTrail;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.repository.AuditTrailRepository;
import ppl.momofin.momofinbackend.repository.specification.AuditTrailSpecifications;

import java.time.LocalDateTime;

@Service
public class AuditTrailServiceImpl implements AuditTrailService {

    private final AuditTrailRepository auditTrailRepository;

    @Autowired
    public AuditTrailServiceImpl(AuditTrailRepository auditTrailRepository) {
        this.auditTrailRepository = auditTrailRepository;
    }

    public Page<AuditTrail> getAuditTrails(
            Organization organization,
            String username,
            String action,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            String documentName,
            Pageable pageable
    ) {
        if (organization == null) {
            throw new IllegalArgumentException("Organization must not be null");
        }

        Specification<AuditTrail> spec = Specification.where(
                        username != null ? AuditTrailSpecifications.hasUser(username) : null)
                .and(action != null ? AuditTrailSpecifications.hasAction(action) : null)
                .and(startDateTime != null ? AuditTrailSpecifications.afterTimestamp(startDateTime) : null)
                .and(endDateTime != null ? AuditTrailSpecifications.beforeTimestamp(endDateTime) : null)
                .and(documentName != null ? AuditTrailSpecifications.hasDocumentName(documentName) : null)
                .and(AuditTrailSpecifications.hasOrganization(organization));

        return auditTrailRepository.findAll(spec, pageable);
    }

    public Integer getTotalCount(
            Organization organization,
            String username,
            String action,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            String documentName
    ) {
        Specification<AuditTrail> spec = Specification.where(
                        username != null ? AuditTrailSpecifications.hasUser(username) : null)
                .and(action != null ? AuditTrailSpecifications.hasAction(action) : null)
                .and(startDateTime != null ? AuditTrailSpecifications.afterTimestamp(startDateTime) : null)
                .and(endDateTime != null ? AuditTrailSpecifications.beforeTimestamp(endDateTime) : null)
                .and(documentName != null ? AuditTrailSpecifications.hasDocumentName(documentName) : null)
                .and(AuditTrailSpecifications.hasOrganization(organization));

        long longCount = auditTrailRepository.count(spec);
        return Math.toIntExact(longCount);
    }

    public void createAuditTrail(AuditTrail auditTrail) {
        auditTrailRepository.save(auditTrail);
    }
}