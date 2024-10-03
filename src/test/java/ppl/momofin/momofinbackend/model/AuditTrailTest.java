package ppl.momofin.momofinbackend.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class AuditTrailTest {

    @Test
    public void testAuditTrailCreation() {
        Document document = new Document("dummy_hash", "dummy_doc");
        User user = new User(new Organization(), "dummy_username", "dummy_name", "dummy@test.com", "dummy_pass", "dummy_manager", true);

        AuditTrail auditTrail = new AuditTrail(document, user, "CREATE", "SUCCESS");

        assertNotNull(auditTrail);
        assertEquals("CREATE", auditTrail.getAction());
        assertEquals("SUCCESS", auditTrail.getAuditOutcome());
        assertEquals(document, auditTrail.getDocument());
        assertEquals(user, auditTrail.getUser());
        assertNotNull(auditTrail.getTimestamp());
    }
}
