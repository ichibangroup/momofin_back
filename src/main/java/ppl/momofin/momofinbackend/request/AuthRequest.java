package ppl.momofin.momofinbackend.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AuthRequest {
    private String organizationName;
    private String username;
    private String password;
}
