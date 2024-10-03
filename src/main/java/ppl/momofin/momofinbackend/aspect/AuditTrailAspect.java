package ppl.momofin.momofinbackend.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ppl.momofin.momofinbackend.model.AuditTrail;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.repository.AuditTrailRepository;

@Aspect
@Component
public class AuditTrailAspect {

    @Autowired
    private AuditTrailRepository auditTrailRepository;

    @Before(value = "execution(* ppl.momofin.momofinbackend.service.DocumentService.*(..)) && args(user, document,..)", argNames = "joinPoint,user,document")
    public void logActivity(JoinPoint joinPoint, User user, Document document) {
        String methodName = joinPoint.getSignature().getName();

        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setUser(user);
        auditTrail.setAction("Method executed: " + methodName);
        auditTrail.setDocument(document);

        auditTrailRepository.save(auditTrail);
    }
}
