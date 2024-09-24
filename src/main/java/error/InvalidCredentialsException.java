package error;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Your email or password is incorrect");
    }
}
