package ppl.momofin.momofinbackend.utility;

import ppl.momofin.momofinbackend.error.InvalidPasswordException;

public class PasswordValidator {
    private PasswordValidator() {
        throw new IllegalStateException("Utility class");
    }

    private static final int MIN_PASSWORD_LENGTH = 10;

    public static void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidPasswordException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters long.");
        }
    }
}
