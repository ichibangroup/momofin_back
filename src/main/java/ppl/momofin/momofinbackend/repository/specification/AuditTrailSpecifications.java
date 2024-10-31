package ppl.momofin.momofinbackend.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import ppl.momofin.momofinbackend.model.AuditTrail;

public class AuditTrailSpecifications {

    public static Specification<AuditTrail> hasAction(String action) {
        return (root, query, cb) -> cb.equal(root.get("action"), action);
    }

    public static Specification<AuditTrail> hasUser(String user) {
        return (root, query, cb) -> cb.equal(root.get("user"), user);
    }
}
