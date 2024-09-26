package ppl.momofin.momofinbackend.service;

import ppl.momofin.momofinbackend.model.AuditTrail;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.repository.AuditTrailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditTrailService {

    @Autowired
    private AuditTrailRepository auditTrailRepository;

    public AuditTrail createAuditTrail(Document document, User user, String action, String verificationResult) {
        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setDocument(document);
        auditTrail.setUser(user);
        auditTrail.setAction(action);
        auditTrail.setVerificationResult(verificationResult);
        auditTrail.setTimestamp(LocalDateTime.now());

        return auditTrailRepository.save(auditTrail);
    }
}
