package ppl.momofin.momofinbackend.aspect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.model.AuditTrail;
import ppl.momofin.momofinbackend.repository.AuditTrailRepository;
import ppl.momofin.momofinbackend.service.UserService;

import static org.mockito.Mockito.*;

public class AuditTrailAspectTest {

    @Mock
    private UserService userService;

    @Mock
    private AuditTrailRepository auditTrailRepository;

    @InjectMocks
    private AuditTrailAspect auditTrailAspect;

    private Document document;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        document = new Document();
        document.setDocumentId(1L);
        document.setName("test.pdf");

        User user = new User();
        user.setUserId(1L);
        user.setUsername("testUser");

        when(userService.fetchUserByUsername("testUser")).thenReturn(user);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken("testUser", "password", null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    public void testCaptureDocumentAfterUpload_Success() {
        auditTrailAspect.captureDocumentAfterUpload(document);

        verify(auditTrailRepository, times(1)).save(any(AuditTrail.class));
    }

    @Test
    public void testCaptureDocumentAfterUpload_Failure_UserNotFound() {
        when(userService.fetchUserByUsername("testUser")).thenReturn(null);

        auditTrailAspect.captureDocumentAfterUpload(document);

        verify(auditTrailRepository, never()).save(any(AuditTrail.class));
    }

    @Test
    public void testCaptureDocumentAfterUpload_Failure_DocumentNull() {
        auditTrailAspect.captureDocumentAfterUpload(null);

        verify(auditTrailRepository, never()).save(any(AuditTrail.class));
    }

    @Test
    public void testCaptureDocumentAfterUpload_Failure_NotAuthenticated() {
        SecurityContextHolder.clearContext();

        auditTrailAspect.captureDocumentAfterUpload(document);

        verify(auditTrailRepository, never()).save(any(AuditTrail.class));
    }
}