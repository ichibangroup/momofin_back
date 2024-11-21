package ppl.momofin.momofinbackend.aspect;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ppl.momofin.momofinbackend.model.AuditTrail;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.repository.AuditTrailRepository;
import ppl.momofin.momofinbackend.service.UserService;

@Aspect
@Component
public class AuditTrailAspect {
    private final UserService userService;

    private final AuditTrailRepository auditTrailRepository;

    @Autowired
    public AuditTrailAspect(UserService userService, AuditTrailRepository auditTrailRepository) {
        this.userService = userService;
        this.auditTrailRepository = auditTrailRepository;
    }

    @Pointcut("execution(* ppl.momofin.momofinbackend.service.CDNService.submitDocument(..))")
    public void documentSubmitPointcut() {}

    @Pointcut("execution(* ppl.momofin.momofinbackend.service.DocumentService.verifyDocument(..))")
    public void documentVerifyPointcut() {}

    @AfterReturning(pointcut = "documentSubmitPointcut()", returning = "document")
    public void captureDocumentAfterSubmit(Document document) {
        captureAuditTrail(document, "SUBMIT");
    }

    @AfterReturning(pointcut = "documentVerifyPointcut()", returning = "document")
    public void captureDocumentAfterVerify(Document document) {
        captureAuditTrail(document, "VERIFY");
    }

    private void captureAuditTrail(Document document, String action) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setAction(action);
        auditTrail.setAuditOutcome("SUCCESS");
        String failed = "FAILED";

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            auditTrail.setAuditOutcome(failed);
            auditTrailRepository.save(auditTrail);
            return;
        }

        String username = authentication.getName();
        User user = userService.fetchUserByUsername(username);
        if (user == null) {
            auditTrail.setAuditOutcome(failed);
            auditTrailRepository.save(auditTrail);
            return;
        }

        if (document == null) {
            auditTrail.setAuditOutcome(failed);
            auditTrailRepository.save(auditTrail);
            return;
        }

        auditTrail.setUser(user);
        auditTrail.setDocument(document);

        auditTrailRepository.save(auditTrail);
    }
}
