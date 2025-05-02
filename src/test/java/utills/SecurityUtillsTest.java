package utills;

import org.junit.jupiter.api.Test;

import utils.SecurityUtills;

import static org.junit.jupiter.api.Assertions.*;

class SecurityUtillsTest {

    @Test
    void testHashPasswordAndVerifySuccess() {
        String plainPassword = "MySecurePassword123!";
        String salt = SecurityUtills.generateSalt();
        String hash = SecurityUtills.hashPassword(plainPassword, salt);

        assertNotNull(hash);
        assertTrue(SecurityUtills.verifyPassword(plainPassword, hash, salt));
    }

    @Test
    void testVerifyFailsWithWrongPassword() {
        String correctPassword = "CorrectPassword";
        String wrongPassword = "WrongPassword";
        String salt = SecurityUtills.generateSalt();
        String hash = SecurityUtills.hashPassword(correctPassword, salt);

        assertFalse(SecurityUtills.verifyPassword(wrongPassword, hash, salt));
    }

    @Test
    void testHashGeneratesDifferentHashesWithDifferentSalts() {
        String password = "SamePassword";
        String salt1 = SecurityUtills.generateSalt();
        String salt2 = SecurityUtills.generateSalt();

        String hash1 = SecurityUtills.hashPassword(password, salt1);
        String hash2 = SecurityUtills.hashPassword(password, salt2);

        assertNotEquals(hash1, hash2);
    }
}
