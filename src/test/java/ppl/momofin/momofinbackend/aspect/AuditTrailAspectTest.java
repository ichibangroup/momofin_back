package ppl.momofin.momofinbackend.aspect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
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
    public void testCaptureDocumentAfterSubmit_SuccessfulAudit() {
        // Mocking an authenticated user and a valid document
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testUser");

        User user = new User(); // Mocked user
        Document document = new Document(); // Mocked document

        when(userService.fetchUserByUsername("testUser")).thenReturn(user);

        // Test the document submission audit
        auditTrailAspect.captureDocumentAfterSubmit(document);

        verify(auditTrailRepository, times(1)).save(any(AuditTrail.class));
    }

    @Test
    public void testCaptureDocumentAfterSubmit_AuthenticationFailed() {
        // Mocking an unauthenticated scenario
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        Document document = new Document(); // Mocked document

        // Test the document submission audit
        auditTrailAspect.captureDocumentAfterSubmit(document);

        verify(auditTrailRepository, times(1)).save(any(AuditTrail.class));
        AuditTrail auditTrail = captureAuditTrail();
        assertEquals("FAILED", auditTrail.getAuditOutcome());
    }

    @Test
    public void testCaptureDocumentAfterVerify_UserNotFound() {
        // Mocking an authenticated user with no corresponding user in the system
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("nonexistentUser");

        Document document = new Document(); // Mocked document
        when(userService.fetchUserByUsername("nonexistentUser")).thenReturn(null);

        // Test the document verification audit
        auditTrailAspect.captureDocumentAfterVerify(document);

        verify(auditTrailRepository, times(1)).save(any(AuditTrail.class));
        AuditTrail auditTrail = captureAuditTrail();
        assertEquals("FAILED", auditTrail.getAuditOutcome());
    }

    @Test
    public void testCaptureDocumentAfterVerify_DocumentNull() {
        // Mocking an authenticated user but with no document passed
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testUser");

        User user = new User(); // Mocked user
        when(userService.fetchUserByUsername("testUser")).thenReturn(user);

        // Test the document verification audit with a null document
        auditTrailAspect.captureDocumentAfterVerify(null);

        verify(auditTrailRepository, times(1)).save(any(AuditTrail.class));
        AuditTrail auditTrail = captureAuditTrail();
        assertEquals("FAILED", auditTrail.getAuditOutcome());
    }

    private AuditTrail captureAuditTrail() {
        // Capture the saved AuditTrail object for further assertions
        var argument = ArgumentCaptor.forClass(AuditTrail.class);
        verify(auditTrailRepository).save(argument.capture());
        return argument.getValue();
    }
}
