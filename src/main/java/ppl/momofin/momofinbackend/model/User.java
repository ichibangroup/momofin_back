package ppl.momofin.momofinbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity @Getter @Setter
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "position", nullable = false)
    private String position;

    @Column(name = "is_organization_admin", nullable = false)
    private boolean isOrganizationAdmin;

    @Column(name = "is_momofin_admin", nullable = false)
    private boolean isMomofinAdmin;

    @ManyToOne
    @JoinColumn(name = "organization", referencedColumnName = "organizationId")
    private Organization organization;

    public User() {

    }

    public User(Organization organization, String name, String email, String password, String position) {
        this.organization = organization;
        this.name = name;
        this.email = email;
        this.password = password;
        this.position = position;
    }

    public User(Organization organization, String name, String email, String password, String position, boolean isOrganizationAdmin) {
        this(organization,name, email, password, position);
        this.isOrganizationAdmin = isOrganizationAdmin;
    }

    public User(Organization organization, String name, String email, String password, String position, boolean isOrganizationAdmin, boolean isMomofinAdmin) {
        this(organization,name, email, password, position, isOrganizationAdmin);
        this.isMomofinAdmin = isMomofinAdmin;
    }
}
