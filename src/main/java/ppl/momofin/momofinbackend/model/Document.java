package ppl.momofin.momofinbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity
public class Document {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;
    private String hashString;
    private String name;

    public Document() {}

    public Document(String hashString, String name) {
        this.hashString = hashString;
        this.name = name;
    }
}
