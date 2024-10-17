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

    @Autowired
    private AuditTrailRepository auditTrailRepository;

    @Autowired
    public AuditTrailAspect(UserService userService, AuditTrailRepository auditTrailRepository) {
        this.userService = userService;
        this.auditTrailRepository = auditTrailRepository;
    }

    @Pointcut("execution(* ppl.momofin.momofinbackend.service.CDNService.uploadFile(..))")
    public void documentSubmitPointcut() {}

    @Pointcut("execution(* ppl.momofin.momofinbackend.service.DocumentService.verifyDocument(..))")
    public void documentVerifyPointcut() {}

    @Pointcut("documentSubmitPointcut() || documentVerifyPointcut()")
    public void documentUploadPointcut() {}

    @AfterReturning(pointcut = "documentUploadPointcut()", returning = "document")
    public void captureDocumentAfterUpload(Document document) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            System.out.println("User is not authenticated, audit trail not saved.");
            return;
        }

        String username = authentication.getName();
        User user = userService.fetchUserByUsername(username);

        if (document == null) {
            System.out.println("Document not found, audit trail not saved.");
            return;
        }

        if (user == null) {
            System.out.println("User not found, audit trail not saved.");
            return;
        }

        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setUser(user);
        auditTrail.setAction("pending");
        auditTrail.setDocument(document);
        auditTrail.setAuditOutcome("SUCCESS");

        auditTrailRepository.save(auditTrail);
    }
}