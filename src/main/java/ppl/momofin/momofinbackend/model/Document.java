package ppl.momofin.momofinbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter @Entity
@Table(name = "document")
public class Document {
    @Column(name = "hash_string")
    private String hashString;
    @Column(name = "name")
    private String name;
    @Id @GeneratedValue(strategy =  GenerationType.UUID)
    private UUID documentId;
    @ManyToOne
    @JoinColumn(name = "owner", referencedColumnName = "userId")
    private User owner;

    public Document() {}

    public Document(String hashString, String name) {
        this.hashString = hashString;
        this.name = name;
    }
}
