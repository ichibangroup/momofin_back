package ppl.momofin.momofinbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
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

    @Column(name = "verificationResult", nullable = false)
    private String verificationResult;

    public AuditTrail() {}

    public AuditTrail(Document document, User user, String action, String verificationResult, LocalDateTime timestamp) {
        this.document = document;
        this.user = user;
        this.action = action;
        this.verificationResult = verificationResult;
        this.timestamp = timestamp;
    }
}
