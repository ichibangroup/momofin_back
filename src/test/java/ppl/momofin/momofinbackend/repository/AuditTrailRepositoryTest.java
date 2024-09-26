package ppl.momofin.momofinbackend.repository;

import ppl.momofin.momofinbackend.model.AuditTrail;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class AuditTrailRepositoryTest {

    @Autowired
    private AuditTrailRepository auditTrailRepository;

    private Document document;
    private User user;

    @BeforeEach
    void setUp() {
        document = new Document();
        document.setName("Test Document");

        user = new User();
        user.setName("Test User");
    }

    @Test
    public void testSaveAuditTrail() {
        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setDocument(document);
        auditTrail.setUser(user);
        auditTrail.setAction("CREATE");
        auditTrail.setVerificationResult("SUCCESS");
        auditTrail.setTimestamp(LocalDateTime.now());

        AuditTrail savedAuditTrail = auditTrailRepository.save(auditTrail);

        assertNotNull(savedAuditTrail.getId());
        assertEquals("CREATE", savedAuditTrail.getAction());
        assertEquals("SUCCESS", savedAuditTrail.getVerificationResult());
        assertEquals("Test Document", savedAuditTrail.getDocument().getName());
        assertEquals("Test User", savedAuditTrail.getUser().getName());
    }
}