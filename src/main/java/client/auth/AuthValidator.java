package client.auth;

/**
 * Provides static validation utilities for authentication input.
 */
public class AuthValidator {

    private static final String EMAIL_REGEX = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";

    private AuthValidator() {
        // Utility class — prevent instantiation
    }

    public static boolean isValidEmailFormat(String email) {
        return email != null && email.matches(EMAIL_REGEX);
    }

    // Optional future methods:
    // public static boolean isStrongPassword(String password) { ... }
}
