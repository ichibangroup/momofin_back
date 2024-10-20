package ppl.momofin.momofinbackend.response;

import lombok.Getter;
import lombok.Setter;
import ppl.momofin.momofinbackend.model.User;

@Getter @Setter
public class AuthResponseSuccess implements Response {
    private String jwt;
    private User user;

    public AuthResponseSuccess() {

    }

    public AuthResponseSuccess(User user, String jwt) {
        this.jwt = jwt;
        this.user = user;
    }
}
