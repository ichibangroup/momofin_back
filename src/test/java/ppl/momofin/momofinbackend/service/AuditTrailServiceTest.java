package ppl.momofin.momofinbackend.service;

import ppl.momofin.momofinbackend.model.AuditTrail;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.repository.AuditTrailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class AuditTrailServiceTest {

    @Mock
    private AuditTrailRepository auditTrailRepository;

    @InjectMocks
    private AuditTrailService auditTrailService;

    private Document document;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up mock Document and User objects
        document = new Document();
        document.setId(1L);
        document.setName("Test Document");

        user = new User();
        user.setId(1L);
        user.setName("Test User");
    }

    @Test
    void testCreateAuditTrail() {
        String action = "CREATE";
        String verificationResult = "SUCCESS";
        LocalDateTime timestamp = LocalDateTime.now();

        AuditTrail mockAuditTrail = new AuditTrail(document, user, action, verificationResult, timestamp);
        when(auditTrailRepository.save(any(AuditTrail.class))).thenReturn(mockAuditTrail);

        AuditTrail result = auditTrailService.createAuditTrail(document, user, action, verificationResult);

        assertNotNull(result);
        assertEquals("CREATE", result.getAction());
        assertEquals("SUCCESS", result.getVerificationResult());
        assertEquals(document.getId(), result.getDocument().getId());
        assertEquals(user.getId(), result.getUser().getId());
        verify(auditTrailRepository, times(1)).save(any(AuditTrail.class));
    }
}
