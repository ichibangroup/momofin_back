package ppl.momofin.momofinbackend.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ppl.momofin.momofinbackend.model.AuditTrail;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@AllArgsConstructor
public class AuditTrailResponse {
    private Long id;
    private String username;
    private String documentName;
    private String action;
    private String date;

    public static AuditTrailResponse fromAuditTrail(AuditTrail audit) {
        DateTimeFormatter dTF = DateTimeFormatter.ofPattern("H:mm • d MMM, uuuu");
        String formattedDate = audit.getTimestamp() != null
                ? audit.getTimestamp().format(dTF)
                : "N/A"; // Handle null timestamp gracefully

        return new AuditTrailResponse(
                audit.getId(),
                audit.getUser() != null ? audit.getUser().getUsername() : "Unknown User", // Handle null user
                audit.getDocument() != null ? audit.getDocument().getName() : "Unknown Document", // Handle null document
                audit.getAction(),
                formattedDate
        );
    }
}