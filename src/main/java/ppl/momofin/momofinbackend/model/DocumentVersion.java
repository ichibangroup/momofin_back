package ppl.momofin.momofinbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Setter
@Getter
public class DocumentVersion {

    @EmbeddedId
    private DocumentVersionKey id;

    private String fileName;
    private String hashString;
    private LocalDateTime createdDate = LocalDateTime.now();

    public DocumentVersion() {
        this.id = new DocumentVersionKey();
        setVersion(1);
    }

    public DocumentVersion(int version, UUID documentId, String fileName, String hashString) {
        this.id = new DocumentVersionKey();
        this.id.setVersion(version);
        this.id.setDocumentId(documentId);
        this.fileName = fileName;
        this.hashString = hashString;
    }

    public int getVersion() {
        return id.getVersion();
    }

    public void setVersion(int version) {
        id.setVersion(version);
    }

    public UUID getDocumentId() {
        return id.getDocumentId();
    }

    public void setDocumentId(UUID documentId) {
        id.setDocumentId(documentId);
    }
}
