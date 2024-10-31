package ppl.momofin.momofinbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ppl.momofin.momofinbackend.model.AuditTrail;
import ppl.momofin.momofinbackend.repository.AuditTrailRepository;

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

    public Page<AuditTrail> getAuditTrails(int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return auditTrailRepository.findAll(pageable);
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