package utils;

import org.junit.jupiter.api.Test;

import utils.SecurityUtils;

import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilsTest {

    @Test
    void testHashPasswordAndVerifySuccess() {
        String plainPassword = "MySecurePassword123!";
        String salt = SecurityUtils.generateSalt();
        String hash = SecurityUtils.hashPassword(plainPassword, salt);

        assertNotNull(hash);
        assertTrue(SecurityUtils.verifyPassword(plainPassword, hash, salt));
    }

    @Test
    void testVerifyFailsWithWrongPassword() {
        String correctPassword = "CorrectPassword";
        String wrongPassword = "WrongPassword";
        String salt = SecurityUtils.generateSalt();
        String hash = SecurityUtils.hashPassword(correctPassword, salt);

        assertFalse(SecurityUtils.verifyPassword(wrongPassword, hash, salt));
    }

    @Test
    void testHashGeneratesDifferentHashesWithDifferentSalts() {
        String password = "SamePassword";
        String salt1 = SecurityUtils.generateSalt();
        String salt2 = SecurityUtils.generateSalt();

        String hash1 = SecurityUtils.hashPassword(password, salt1);
        String hash2 = SecurityUtils.hashPassword(password, salt2);

        assertNotEquals(hash1, hash2);
    }
}
