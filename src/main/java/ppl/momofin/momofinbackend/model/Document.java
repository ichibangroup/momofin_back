package ppl.momofin.momofinbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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
    @JoinColumn(name = "owner", referencedColumnName = "userId")
    private User owner;
    @OneToMany(mappedBy = "id.document", cascade = CascadeType.ALL)
    private List<DocumentVersion> versions;


    @OneToOne
    private DocumentVersion currentVersion;

    public Document() {
        versions = new ArrayList<>();
    }

    public Document(String hashString, String name) {
        this.hashString = hashString;
        this.name = name;
    }
}
