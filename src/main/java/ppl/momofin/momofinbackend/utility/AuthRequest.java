package ppl.momofin.momofinbackend.utility;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AuthRequest {
    private String organizationName;
    private String email;
    private String password;
}