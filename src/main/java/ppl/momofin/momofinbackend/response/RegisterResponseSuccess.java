package ppl.momofin.momofinbackend.response;

import lombok.Getter;
import lombok.Setter;
import ppl.momofin.momofinbackend.model.User;

@Getter @Setter
public class RegisterResponseSuccess implements Response {
    private User user;

    public RegisterResponseSuccess() {}

    public RegisterResponseSuccess(User user) {
        this.user = user;
    }
}
