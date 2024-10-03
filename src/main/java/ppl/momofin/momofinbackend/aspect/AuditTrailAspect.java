package ppl.momofin.momofinbackend.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ppl.momofin.momofinbackend.model.AuditTrail;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.service.UserService;
import ppl.momofin.momofinbackend.repository.AuditTrailRepository;

@Aspect
@Component
public class AuditTrailAspect {
    private final UserService userService;

    @Autowired
    private AuditTrailRepository auditTrailRepository;

    public AuditTrailAspect(UserService userService) {
        this.userService = userService;
    }

    @AfterReturning(pointcut = "execution(* ppl.momofin.momofinbackend.service.DocumentService.verifyDocument(..))", returning = "document")
    public void logDocumentVerifyReturn(JoinPoint joinPoint, Document document) {
        logActivity(joinPoint, document, "VERIFY");
    }

    private void logActivity(JoinPoint joinPoint, Document document, String action) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = null;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            username = authentication.getName();
        }

        User user = userService.fetchUserByUsername(username);
        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setUser(user);
        auditTrail.setAction(action);
        auditTrail.setDocument(document);
        auditTrail.setAuditOutcome(document != null ? "SUCCESS" : "FAILED");

        auditTrailRepository.save(auditTrail);
    }
}
