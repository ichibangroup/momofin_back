package ppl.momofin.momofinbackend.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import ppl.momofin.momofinbackend.model.AuditTrail;

import java.time.LocalDateTime;

public class AuditTrailSpecifications {

    public static Specification<AuditTrail> hasAction(String action) {
        return (root, query, cb) ->
                action == null ? cb.conjunction() : cb.equal(root.get("action"), action);
    }

    public static Specification<AuditTrail> hasUser(String username) {
        return (root, query, cb) ->
                username == null || username.trim().isEmpty() ? cb.conjunction() : cb.like(cb.lower(root.get("user").get("username")), "%" + username.toLowerCase() + "%");
    }

    public static Specification<AuditTrail> afterTimestamp(LocalDateTime startDateTime) {
        return (root, query, cb) ->
                startDateTime == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("timestamp"), startDateTime);
    }

    public static Specification<AuditTrail> beforeTimestamp(LocalDateTime endDateTime) {
        return (root, query, cb) ->
                endDateTime == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("timestamp"), endDateTime);
    }

    public static Specification<AuditTrail> hasDocumentName(String documentName) {
        return (root, query, cb) ->
                documentName == null || documentName.trim().isEmpty() ? cb.conjunction() : cb.like(cb.lower(root.get("document").get("name")), "%" + documentName.toLowerCase() + "%");
    }
}
