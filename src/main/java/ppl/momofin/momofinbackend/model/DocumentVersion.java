package ppl.momofin.momofinbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;

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

    public DocumentVersion(int version, Document document, String fileName, String hashString) {
        this.id = new DocumentVersionKey();
        this.id.setVersion(version);
        this.id.setDocument(document);
        this.fileName = fileName;
        this.hashString = hashString;
    }

    public int getVersion() {
        return id.getVersion();
    }

    public void setVersion(int version) {
        id.setVersion(version);
    }

    public Document getDocument() {
        return id.getDocument();
    }

    public void setDocument(Document document) {
        id.setDocument(document);
    }
}
