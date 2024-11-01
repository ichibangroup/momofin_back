package ppl.momofin.momofinbackend.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ppl.momofin.momofinbackend.model.AuditTrail;

import java.time.format.DateTimeFormatter;

@Getter @Setter @AllArgsConstructor
public class AuditTrailResponse {
    private Long id;
    private String username;
    private String document;
    private String action;
    private String date;

    public static AuditTrailResponse fromAuditTrail(AuditTrail audit) {
        DateTimeFormatter dTF = DateTimeFormatter.ofPattern("H:mm • d MMM, uuuu");
        String formattedDate = audit.getTimestamp().format(dTF);

        return new AuditTrailResponse(
                audit.getId(),
                audit.getUser().getUsername(),
                audit.getDocument().getName(),
                audit.getAction(),
                formattedDate
        );
    }
}
