package ppl.momofin.momofinbackend.response;

import org.junit.jupiter.api.Test;
import ppl.momofin.momofinbackend.model.AuditTrail;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuditTrailResponseTest {

    @Test
    void fromAuditTrail_shouldMapFieldsCorrectly() {
        User user = new User();
        user.setUsername("testUser");

        Document document = new Document();
        document.setName("testDocument");

        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setId(1L);
        auditTrail.setUser(user);
        auditTrail.setDocument(document);
        auditTrail.setAction("SUBMIT");
        auditTrail.setTimestamp(LocalDateTime.of(2023, 1, 1, 12, 0));

        AuditTrailResponse response = AuditTrailResponse.fromAuditTrail(auditTrail);

        assertEquals(1L, response.getId());
        assertEquals("testUser", response.getUsername());
        assertEquals("testDocument", response.getDocumentName());
        assertEquals("SUBMIT", response.getAction());
        assertEquals("12:00 • 1 Jan, 2023", response.getDate());
    }

    @Test
    void fromAuditTrail_shouldHandleNullUserGracefully() {
        Document document = new Document();
        document.setName("testDocument");

        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setId(1L);
        auditTrail.setUser(null);
        auditTrail.setDocument(document);
        auditTrail.setAction("SUBMIT");
        auditTrail.setTimestamp(LocalDateTime.of(2023, 1, 1, 12, 0));

        AuditTrailResponse response = AuditTrailResponse.fromAuditTrail(auditTrail);

        assertEquals("Unknown User", response.getUsername());
        assertEquals("testDocument", response.getDocumentName());
    }

    @Test
    void fromAuditTrail_shouldHandleNullDocumentGracefully() {
        User user = new User();
        user.setUsername("testUser");

        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setId(1L);
        auditTrail.setUser(user);
        auditTrail.setDocument(null);
        auditTrail.setAction("SUBMIT");
        auditTrail.setTimestamp(LocalDateTime.of(2023, 1, 1, 12, 0));

        AuditTrailResponse response = AuditTrailResponse.fromAuditTrail(auditTrail);

        assertEquals("testUser", response.getUsername());
        assertEquals("Unknown Document", response.getDocumentName());
    }

    @Test
    void fromAuditTrail_shouldHandleNullTimestampGracefully() {
        User user = new User();
        user.setUsername("testUser");

        Document document = new Document();
        document.setName("testDocument");

        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setId(1L);
        auditTrail.setUser(user);
        auditTrail.setDocument(document);
        auditTrail.setAction("SUBMIT");
        auditTrail.setTimestamp(null);

        AuditTrailResponse response = AuditTrailResponse.fromAuditTrail(auditTrail);

        assertEquals("N/A", response.getDate());
    }
}