package ppl.momofin.momofinbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity
@Table(name = "document")
public class Document {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;
    @Column(name = "hash_string")
    private String hashString;
    @Column(name = "name")
    private String name;
    @ManyToOne
    @JoinColumn(name = "users", referencedColumnName = "userId")
    private User owner;

    public Document() {}

    public Document(String hashString, String name) {
        this.hashString = hashString;
        this.name = name;
    }
}
