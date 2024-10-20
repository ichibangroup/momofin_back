package ppl.momofin.momofinbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter @Setter @Entity
@Table(name = "organization")
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long organizationId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    @ColumnDefault("'Default organization description'")
    private String description;

    public Organization() {}

    public Organization(String name) {
        this.name = name;
        this.description = "Default organization description";
    }

    public Organization(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
