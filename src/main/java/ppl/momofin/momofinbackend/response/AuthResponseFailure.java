package ppl.momofin.momofinbackend.response;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AuthResponseFailure implements AuthResponse{
    private String errorMessage;

    public AuthResponseFailure() {

    }

    public AuthResponseFailure(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
