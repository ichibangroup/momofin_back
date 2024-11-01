package ppl.momofin.momofinbackend.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import ppl.momofin.momofinbackend.model.AuditTrail;
import ppl.momofin.momofinbackend.model.User;

import java.time.LocalDateTime;

public class AuditTrailSpecifications {

    public static Specification<AuditTrail> hasAction(String action) {
        return (root, query, cb) -> cb.equal(root.get("action"), action);
    }

    public static Specification<AuditTrail> hasUser(User user) {
        return (root, query, cb) -> cb.equal(root.get("user"), user);
    }

    public static Specification<AuditTrail> afterTimestamp(LocalDateTime startDateTime) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("timestamp"), startDateTime);
    }

    public static Specification<AuditTrail> beforeTimestamp(LocalDateTime endDateTime) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("timestamp"), endDateTime);
    }

}
