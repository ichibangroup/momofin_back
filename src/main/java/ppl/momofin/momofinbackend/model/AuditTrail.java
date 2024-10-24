package ppl.momofin.momofinbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Table(name = "audit_trail")
public class AuditTrail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "audit_outcome", nullable = false)
    private String auditOutcome;

    public AuditTrail() {
        this.timestamp = LocalDateTime.now();
    }

    public AuditTrail(Document document, User user, String action, String auditOutcome) {
        this();
        this.document = document;
        this.user = user;
        this.action = action;
        this.auditOutcome = auditOutcome;
    }
}