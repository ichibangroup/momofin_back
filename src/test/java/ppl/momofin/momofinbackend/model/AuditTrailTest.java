package ppl.momofin.momofinbackend.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class AuditTrailTest {

    @Test
    public void testAuditTrailCreation() {
        Document document = new Document(1, "docHash", "Sample Doc", LocalDateTime.now(), null);
        User user = new User(1, "user1@example.com", "User One", true, false);
        LocalDateTime timestamp = LocalDateTime.now();

        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setDocument(document);
        auditTrail.setUser(user);
        auditTrail.setAction("CREATE");
        auditTrail.setVerificationResult("SUCCESS");
        auditTrail.setTimestamp(timestamp);

        assertNotNull(auditTrail);
        assertEquals("CREATE", auditTrail.getAction());
        assertEquals("SUCCESS", auditTrail.getVerificationResult());
        assertEquals(timestamp, auditTrail.getTimestamp());
        assertEquals(document, auditTrail.getDocument());
        assertEquals(user, auditTrail.getUser());
    }
}
