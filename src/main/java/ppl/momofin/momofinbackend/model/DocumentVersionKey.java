package ppl.momofin.momofinbackend.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter @Setter @Embeddable
public class DocumentVersionKey implements Serializable {
    private UUID documentId;
    private int version;

    // Default constructor, getters, setters, equals, and hashCode
    public DocumentVersionKey() {}

    public DocumentVersionKey(UUID documentId, int version) {
        this.documentId = documentId;
        this.version = version;
    }
}
