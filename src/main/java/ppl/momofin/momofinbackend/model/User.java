package ppl.momofin.momofinbackend.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "companyname", nullable = false)
    private String companyname;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    public User(String companyname, String email, String password) {
        this.companyname = companyname;
        this.email = email;
        this.password = password;
    }

    public User() {}
}
