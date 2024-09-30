package ppl.momofin.momofinbackend.response;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ErrorResponse implements AuthResponse{
    private String errorMessage;

    public ErrorResponse() {

    }

    public ErrorResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
