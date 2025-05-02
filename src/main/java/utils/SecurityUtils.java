// inspiration from https://stackoverflow.com/questions/69791042/how-to-validate-user-password-after-hashing-using-sha-256-salted-in-java

package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class SecurityUtils {

    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(Constants.HASH_ALGORITHM);
            digest.update(salt.getBytes());
            byte[] hashedBytes = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            LogHandler.log("Hashing failed: " + e.getMessage());
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public static String generateSalt() {
        byte[] salt = new byte[Constants.SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static boolean verifyPassword(String plainPassword, String storedHashedPassword, String salt) {
        String hashedInput = hashPassword(plainPassword, salt);
        return hashedInput.equals(storedHashedPassword);
    }
}
