package utility;

import error.InvalidPasswordException;

public class PasswordValidator {
    private static final int MIN_PASSWORD_LENGTH = 10;

    public static void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidPasswordException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters long.");
        }
    }
}
