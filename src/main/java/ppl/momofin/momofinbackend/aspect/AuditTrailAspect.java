package ppl.momofin.momofinbackend.aspect;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ppl.momofin.momofinbackend.model.AuditTrail;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.service.AuditTrailService;
import ppl.momofin.momofinbackend.service.UserService;

@Aspect
@Component
public class AuditTrailAspect {

    private final UserService userService;
    private final AuditTrailService auditTrailService;

    public AuditTrailAspect(UserService userService, AuditTrailService auditTrailService) {
        this.userService = userService;
        this.auditTrailService = auditTrailService;
    }

    @Pointcut("execution(* ppl.momofin.momofinbackend.service.CDNService.submitDocument(..))")
    public void documentSubmitPointcut() {}

    @Pointcut("execution(* ppl.momofin.momofinbackend.service.DocumentService.verifyDocument(..))")
    public void documentVerifyPointcut() {}

    @AfterReturning(pointcut = "documentSubmitPointcut()", returning = "document")
    public void captureDocumentAfterSubmit(Document document) {
        createAuditTrail(document, "SUBMIT");
    }

    @AfterReturning(pointcut = "documentVerifyPointcut()", returning = "document")
    public void captureDocumentAfterVerify(Document document) {
        createAuditTrail(document, "VERIFY");
    }

    private void createAuditTrail(Document document, String action) {
        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setAction(action);
        auditTrail.setAuditOutcome("SUCCESS");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!isAuthenticated(authentication)) {
            auditTrail.setAuditOutcome("FAILED");
        } else {
            String username = authentication.getName();
            User user = userService.fetchUserByUsername(username);
            if (user != null) {
                auditTrail.setUser(user);
            } else {
                auditTrail.setAuditOutcome("FAILED");
            }
        }

        if (document != null) {
            auditTrail.setDocument(document);
        } else {
            auditTrail.setAuditOutcome("FAILED");
        }

        auditTrailService.createAuditTrail(auditTrail);
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
    }
}