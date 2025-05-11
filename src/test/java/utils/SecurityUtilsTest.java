package utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilsTest {

    @Test
    void testGenerateSaltProducesUniqueNonNullString() {
        String salt1 = SecurityUtils.generateSalt();
        String salt2 = SecurityUtils.generateSalt();

        assertNotNull(salt1);
        assertNotNull(salt2);
        assertNotEquals(salt1, salt2);
        assertFalse(salt1.isBlank());
        assertFalse(salt2.isBlank());
    }

    @Test
    void testHashPasswordProducesNonNullHash() {
        String salt = SecurityUtils.generateSalt();
        String hash = SecurityUtils.hashPassword("password123", salt);

        assertNotNull(hash);
        assertFalse(hash.isBlank());
        assertNotEquals("password123", hash); // Ensure it's not the plain password
    }

    @Test
    void testVerifyPasswordSuccess() {
        String password = "mySecretPass";
        String salt = SecurityUtils.generateSalt();
        String hash = SecurityUtils.hashPassword(password, salt);

        assertTrue(SecurityUtils.verifyPassword(password, hash, salt));
    }

    @Test
    void testVerifyPasswordFailsWithWrongPassword() {
        String salt = SecurityUtils.generateSalt();
        String hash = SecurityUtils.hashPassword("correctPassword", salt);

        assertFalse(SecurityUtils.verifyPassword("wrongPassword", hash, salt));
    }

    @Test
    void testVerifyPasswordFailsWithWrongSalt() {
        String password = "securePassword";
        String salt1 = SecurityUtils.generateSalt();
        String salt2 = SecurityUtils.generateSalt();

        String hash = SecurityUtils.hashPassword(password, salt1);
        assertFalse(SecurityUtils.verifyPassword(password, hash, salt2));
    }
}
