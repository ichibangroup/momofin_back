package ppl.momofin.momofinbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import ppl.momofin.momofinbackend.utility.Roles;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity @Getter @Setter
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email")
    private String email;

    @JsonIgnore
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "position")
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

    public User(Organization organization, String username, String name, String email, String password, String position, Roles roles) {
        this(organization, username, name, email, password, position);
        this.isOrganizationAdmin = roles.isOrganizationalAdmin();
        this.isMomofinAdmin = roles.isMomofinAdmin();
    }

    public Set<String> getRoles() {
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");  // All users have the USER role
        if (isOrganizationAdmin) {
            roles.add("ROLE_ORG_ADMIN");
        }
        if (isMomofinAdmin) {
            roles.add("ROLE_MOMOFIN_ADMIN");
        }
        return roles;
    }
}
