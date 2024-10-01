package ppl.momofin.momofinbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity @Getter @Setter
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false)
    private String email;

    @JsonIgnore
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "position", nullable = false)
    private String position;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "is_organization_admin", nullable = false)
    private boolean isOrganizationAdmin;

    @Column(name = "is_momofin_admin", nullable = false)
    private boolean isMomofinAdmin;

    @ManyToOne
    @JoinColumn(name = "organization", referencedColumnName = "organizationId")
    private Organization organization;

    public User() {

    }

    public User(Organization organization, String username, String name, String email, String password, String position) {
        this.organization = organization;
        this.username = username;
        this.name = name;
        this.email = email;
        this.password = password;
        this.position = position;
    }

    public User(Organization organization, String username, String name, String email, String password, String position, boolean isOrganizationAdmin) {
        this(organization, username,name, email, password, position);
        this.isOrganizationAdmin = isOrganizationAdmin;
    }

    public User(Organization organization, String username, String name, String email, String password, String position, boolean isOrganizationAdmin, boolean isMomofinAdmin) {
        this(organization, username, name, email, password, position, isOrganizationAdmin);
        this.isMomofinAdmin = isMomofinAdmin;
    }
}
