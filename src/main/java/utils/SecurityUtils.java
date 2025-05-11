package utils;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import static utils.ServerConstants.*;

/**
 * Utility class for handling password hashing, salting, and verification logic
 * using SHA-256 and secure random generation.
 * <p>
 * Supports centralized security operations for AuthService.
 */
@Slf4j
public class SecurityUtils {

    /**
     * Generates a cryptographically secure random salt.
     *
     * @return base64-encoded salt string
     */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes a password with a given salt using SHA-256.
     *
     * @param password Raw user password
     * @param salt     Salt string to prepend before hashing
     * @return Base64-encoded hash string
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            digest.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashed = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (Exception e) {
            log.error("Hashing error: {}", e.getMessage());
            LogHandler.log("Hashing error: " + e.getMessage());
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    /**
     * Verifies a raw password by comparing its salted hash to a stored hash.
     *
     * @param rawPassword Raw password input
     * @param storedHash  Hash stored in the database
     * @param salt        Salt used during hashing
     * @return true if hashes match, false otherwise
     */
    public static boolean verifyPassword(String rawPassword, String storedHash, String salt) {
        String hashedAttempt = hashPassword(rawPassword, salt);
        boolean match = MessageDigest.isEqual(
                hashedAttempt.getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8));

        if (!match) {
            log.warn("Password verification failed");
        } else if (DEBUG_MODE) {
            log.debug("Password verified for salt: {}", salt);
        }

        return match;
    }

    private SecurityUtils() {
        // Prevent instantiation of utility class
    }
}
