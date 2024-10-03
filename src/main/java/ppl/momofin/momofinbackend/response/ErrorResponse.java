package ppl.momofin.momofinbackend.response;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ErrorResponse implements Response {
    private String errorMessage;

    public ErrorResponse() {

    }

    public ErrorResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
