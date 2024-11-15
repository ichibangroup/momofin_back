package ppl.momofin.momofinbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter @Entity
@Table(name = "document")
public class Document {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID documentId;
    @Column(name = "hash_string")
    private String hashString;
    @Column(name = "name")
    private String name;
    @ManyToOne
    @JoinColumn(name = "owner", referencedColumnName = "userId")
    private User owner;
    private int currentVersion;
    private boolean isBeingRequested;

    public Document() {}

    public Document(String hashString, String name) {
        this.hashString = hashString;
        this.name = name;
        this.currentVersion = 1;
    }

    public Document(String hashString, String name, int currentVersion) {
        this.hashString = hashString;
        this.name = name;
        this.currentVersion = currentVersion;
    }
}
