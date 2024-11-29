package ppl.momofin.momofinbackend.aspect;

import org.aspectj.lang.annotation.Aspect;
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
import ppl.momofin.momofinbackend.service.AuditTrailService;
import ppl.momofin.momofinbackend.service.UserService;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Aspect
class AuditTrailAspectTest {

    @Mock
    private UserService userService;

    @Mock
    private AuditTrailService auditTrailService;

    @InjectMocks
    private AuditTrailAspect auditTrailAspect;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void shouldCreateAuditTrailForDocumentSubmission() {
        Document document = new Document();
        User user = new User();
        user.setUsername("testUser");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testUser");
        when(userService.fetchUserByUsername("testUser")).thenReturn(user);

        auditTrailAspect.captureDocumentAfterSubmit(document);

        ArgumentCaptor<AuditTrail> captor = ArgumentCaptor.forClass(AuditTrail.class);
        verify(auditTrailService).createAuditTrail(captor.capture());

        AuditTrail capturedAuditTrail = captor.getValue();
        assertEquals("SUBMIT", capturedAuditTrail.getAction());
        assertEquals("SUCCESS", capturedAuditTrail.getAuditOutcome());
        assertEquals(user, capturedAuditTrail.getUser());
        assertEquals(document, capturedAuditTrail.getDocument());
    }

    @Test
    void shouldCreateAuditTrailWithFailedOutcomeForUnauthenticatedUser() {
        Document document = new Document();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        auditTrailAspect.captureDocumentAfterSubmit(document);

        ArgumentCaptor<AuditTrail> captor = ArgumentCaptor.forClass(AuditTrail.class);
        verify(auditTrailService).createAuditTrail(captor.capture());

        AuditTrail capturedAuditTrail = captor.getValue();
        assertEquals("SUBMIT", capturedAuditTrail.getAction());
        assertEquals("FAILED", capturedAuditTrail.getAuditOutcome());
        assertNull(capturedAuditTrail.getUser());
    }

    @Test
    void shouldCreateAuditTrailWithFailedOutcomeForAnonymousUser() {
        Document document = new Document();
        AnonymousAuthenticationToken anonymousAuth = mock(AnonymousAuthenticationToken.class);

        when(securityContext.getAuthentication()).thenReturn(anonymousAuth);

        auditTrailAspect.captureDocumentAfterSubmit(document);

        ArgumentCaptor<AuditTrail> captor = ArgumentCaptor.forClass(AuditTrail.class);
        verify(auditTrailService).createAuditTrail(captor.capture());

        AuditTrail capturedAuditTrail = captor.getValue();
        assertEquals("SUBMIT", capturedAuditTrail.getAction());
        assertEquals("FAILED", capturedAuditTrail.getAuditOutcome());
        assertNull(capturedAuditTrail.getUser());
    }

    @Test
    void shouldCreateAuditTrailWithFailedOutcomeForNullUser() {
        Document document = mock(Document.class);
        User user = new User();
        user.setUsername("testUser");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testUser");
        when(userService.fetchUserByUsername("testUser")).thenReturn(user);

        when(userService.fetchUserByUsername(any(String.class))).thenReturn(null);

        auditTrailAspect.captureDocumentAfterSubmit(document);

        ArgumentCaptor<AuditTrail> auditTrailCaptor = ArgumentCaptor.forClass(AuditTrail.class);
        verify(auditTrailService, times(1)).createAuditTrail(auditTrailCaptor.capture());

        AuditTrail capturedAuditTrail = auditTrailCaptor.getValue();
        assertEquals("FAILED", capturedAuditTrail.getAuditOutcome());
        assertNull(capturedAuditTrail.getUser()); // No user should be set
        assertNotNull(capturedAuditTrail.getDocument()); // The document should still be set
    }

    @Test
    void shouldCreateAuditTrailWithFailedOutcomeForNullDocument() {
        User user = new User();
        user.setUsername("testUser");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testUser");
        when(userService.fetchUserByUsername("testUser")).thenReturn(user);

        auditTrailAspect.captureDocumentAfterSubmit(null);

        ArgumentCaptor<AuditTrail> captor = ArgumentCaptor.forClass(AuditTrail.class);
        verify(auditTrailService).createAuditTrail(captor.capture());

        AuditTrail capturedAuditTrail = captor.getValue();
        assertEquals("SUBMIT", capturedAuditTrail.getAction());
        assertEquals("FAILED", capturedAuditTrail.getAuditOutcome());
        assertEquals(user, capturedAuditTrail.getUser());
        assertNull(capturedAuditTrail.getDocument());
    }

    @Test
    void shouldCreateAuditTrailForDocumentVerification() {
        Document document = new Document();
        User user = new User();
        user.setUsername("testUser");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testUser");
        when(userService.fetchUserByUsername("testUser")).thenReturn(user);

        auditTrailAspect.captureDocumentAfterVerify(document);

        ArgumentCaptor<AuditTrail> captor = ArgumentCaptor.forClass(AuditTrail.class);
        verify(auditTrailService).createAuditTrail(captor.capture());

        AuditTrail capturedAuditTrail = captor.getValue();
        assertEquals("VERIFY", capturedAuditTrail.getAction());
        assertEquals("SUCCESS", capturedAuditTrail.getAuditOutcome());
        assertEquals(user, capturedAuditTrail.getUser());
        assertEquals(document, capturedAuditTrail.getDocument());
    }
}