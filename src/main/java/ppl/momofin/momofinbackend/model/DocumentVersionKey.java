package ppl.momofin.momofinbackend.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter @Setter @Embeddable
public class DocumentVersionKey implements Serializable {
    @ManyToOne
    private Document document;
    private int version;

    // Default constructor, getters, setters, equals, and hashCode
    public DocumentVersionKey() {}

    public DocumentVersionKey(Document document, int version) {
        this.document = document;
        this.version = version;
    }
}
