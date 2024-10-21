package ppl.momofin.momofinbackend.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuditTrailTest {

    @Test
    void testDefaultConstructor() {
        AuditTrail auditTrail = new AuditTrail();

        assertNotNull(auditTrail.getTimestamp());
        assertTrue(auditTrail.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void testParameterizedConstructor() {
        Document document = new Document();
        User user = new User();
        String action = "CREATE";
        String auditOutcome = "SUCCESS";

        AuditTrail auditTrail = new AuditTrail(document, user, action, auditOutcome);

        assertEquals(document, auditTrail.getDocument());
        assertEquals(user, auditTrail.getUser());
        assertEquals(action, auditTrail.getAction());
        assertEquals(auditOutcome, auditTrail.getAuditOutcome());
        assertNotNull(auditTrail.getTimestamp());
    }

    @Test
    void testSettersAndGetters() {
        AuditTrail auditTrail = new AuditTrail();
        Document document = new Document();
        User user = new User();

        auditTrail.setDocument(document);
        auditTrail.setUser(user);
        auditTrail.setAction("UPDATE");
        auditTrail.setAuditOutcome("FAILURE");

        assertEquals(document, auditTrail.getDocument());
        assertEquals(user, auditTrail.getUser());
        assertEquals("UPDATE", auditTrail.getAction());
        assertEquals("FAILURE", auditTrail.getAuditOutcome());
    }
}
