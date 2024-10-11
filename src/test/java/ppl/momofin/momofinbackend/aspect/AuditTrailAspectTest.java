package ppl.momofin.momofinbackend.aspect;

import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ppl.momofin.momofinbackend.model.AuditTrail;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.repository.AuditTrailRepository;
import ppl.momofin.momofinbackend.service.UserService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest
public class AuditTrailAspectTest {

    @InjectMocks
    private AuditTrailAspect auditTrailAspect;

    @Mock
    private UserService userService;

    @Mock
    private AuditTrailRepository auditTrailRepository;

    @Mock
    private JoinPoint joinPoint;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up a mock authentication in the SecurityContext
        Authentication authentication = new UsernamePasswordAuthenticationToken("testUser", "password");
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    public void testLogDocumentVerifyReturn_Success() {
        Document document = new Document();
        document.setDocumentId(1L);

        User mockUser = new User();
        mockUser.setUserId(1L);
        when(userService.fetchUserByUsername(anyString())).thenReturn(mockUser);

        auditTrailAspect.logDocumentVerifyReturn(joinPoint, document);

        ArgumentCaptor<AuditTrail> auditTrailCaptor = ArgumentCaptor.forClass(AuditTrail.class);
        verify(auditTrailRepository, times(1)).save(auditTrailCaptor.capture());


        AuditTrail capturedAuditTrail = auditTrailCaptor.getValue();
        assertNotNull(capturedAuditTrail);
        assertEquals("VERIFY", capturedAuditTrail.getAction());
        assertEquals(document, capturedAuditTrail.getDocument());
        assertEquals("SUCCESS", capturedAuditTrail.getAuditOutcome());
    }

    @Test
    public void testLogDocumentVerifyReturn_Failure() {
        Document document = null;

        User mockUser = new User();
        mockUser.setUsername("testUser");
        when(userService.fetchUserByUsername("testUser")).thenReturn(mockUser);

        auditTrailAspect.logDocumentVerifyReturn(joinPoint, document);

        ArgumentCaptor<AuditTrail> auditTrailCaptor = ArgumentCaptor.forClass(AuditTrail.class);
        verify(auditTrailRepository, times(1)).save(auditTrailCaptor.capture());

        AuditTrail capturedAuditTrail = auditTrailCaptor.getValue();
        assertNotNull(capturedAuditTrail);
        assertEquals("VERIFY", capturedAuditTrail.getAction());
        assertNull(capturedAuditTrail.getDocument());
        assertEquals("FAILED", capturedAuditTrail.getAuditOutcome());
    }
}
