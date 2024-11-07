package ppl.momofin.momofinbackend.dto;
import lombok.*;
import ppl.momofin.momofinbackend.model.EditRequest;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class EditRequestDTO {
    private String documentId;
    private String userId;
    private String organizationName;
    private String username;
    private String position;
    private String email;
    private String documentName;

    public EditRequestDTO(UUID documentId, UUID userId, String organizationName, String username, String position, String email, String documentName) {
        this.documentId = documentId.toString();
        this.userId = userId.toString();
        this.organizationName = organizationName;
        this.username = username;
        this.position = position;
        this.email = email;
        this.documentName = documentName;
    }

    public static EditRequestDTO toDTO(EditRequest editRequest) {
        EditRequestDTO dto = new EditRequestDTO();
        dto.setDocumentId(editRequest.getDocumentId().toString());
        dto.setUserId(editRequest.getUserId().toString());
        dto.setOrganizationName(editRequest.getDocument().getOwner().getOrganization().getName());
        dto.setUsername(editRequest.getDocument().getOwner().getUsername());
        dto.setPosition(editRequest.getDocument().getOwner().getPosition());
        dto.setEmail(editRequest.getDocument().getOwner().getEmail());
        dto.setDocumentName(editRequest.getDocument().getName());
        return dto;
    }
}
