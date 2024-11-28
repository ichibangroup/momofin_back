package ppl.momofin.momofinbackend.aspect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ppl.momofin.momofinbackend.model.AuditTrail;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.repository.AuditTrailRepository;
import ppl.momofin.momofinbackend.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuditTrailAspectTest {

    @Mock
    private UserService userService;

    @Mock
    private AuditTrailRepository auditTrailRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuditTrailAspect auditTrailAspect;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testCaptureDocumentAfterSubmit_SuccessfulAudit() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testUser");

        User user = new User();
        Document document = new Document();

        when(userService.fetchUserByUsername("testUser")).thenReturn(user);

        auditTrailAspect.captureDocumentAfterSubmit(document);

        verify(auditTrailRepository, times(1)).save(any(AuditTrail.class));
    }

    @Test
    void testCaptureDocumentAfterSubmit_AuthenticationFailed() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        Document document = new Document();

        auditTrailAspect.captureDocumentAfterSubmit(document);

        verify(auditTrailRepository, times(1)).save(any(AuditTrail.class));
        AuditTrail auditTrail = captureAuditTrail();
        assertEquals("FAILED", auditTrail.getAuditOutcome());
    }

    @Test
    void testCaptureDocumentAfterVerify_UserNotFound() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("nonexistentUser");

        Document document = new Document();
        when(userService.fetchUserByUsername("nonexistentUser")).thenReturn(null);

        auditTrailAspect.captureDocumentAfterVerify(document);

        verify(auditTrailRepository, times(1)).save(any(AuditTrail.class));
        AuditTrail auditTrail = captureAuditTrail();
        assertEquals("FAILED", auditTrail.getAuditOutcome());
    }

    @Test
    void testCaptureDocumentAfterVerify_DocumentNull() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testUser");

        User user = new User();
        when(userService.fetchUserByUsername("testUser")).thenReturn(user);

        auditTrailAspect.captureDocumentAfterVerify(null);

        verify(auditTrailRepository, times(1)).save(any(AuditTrail.class));
        AuditTrail auditTrail = captureAuditTrail();
        assertEquals("FAILED", auditTrail.getAuditOutcome());
    }

    private AuditTrail captureAuditTrail() {
        var argument = ArgumentCaptor.forClass(AuditTrail.class);
        verify(auditTrailRepository).save(argument.capture());
        return argument.getValue();
    }
}