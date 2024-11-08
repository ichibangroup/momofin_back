package ppl.momofin.momofinbackend.error;

public class SecurityValidationException extends RuntimeException {
    public SecurityValidationException(String message) {
        super(message);
    }
}
