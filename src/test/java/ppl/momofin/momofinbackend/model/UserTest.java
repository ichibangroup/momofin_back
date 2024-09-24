package ppl.momofin.momofinbackend.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserTest {
    private String companyName;
    private String password;
    private String email;
    private User user;

    @BeforeEach
    public void setup() {
        this.companyName = "companyName";
        this.email = "email";
        this.password = "password";
        this.user = new User();
    }

    @Test
    public void testLogin() {
        User user1 = new User(companyName, email, password);

        assertEquals("companyName", user1.getCompanyName());
        assertEquals("email", user1.getEmail());
        assertEquals("password", user1.getPassword());
    }

    @Test
    public void testGetSetCompanyName() {
        user.setCompanyName(companyName);

        assertEquals("companyName", user.getCompanyName());
    }
    @Test
    public void testGetSetEmail() {
        user.setEmail(email);

        assertEquals("companyName", user.getEmail());
    }

    @Test
    public void testGetSetPassword() {
        user.setPassword(password);

        assertEquals("password", user.getPassword());
    }
}
