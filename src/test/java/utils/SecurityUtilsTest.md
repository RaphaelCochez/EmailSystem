# Security Utilities Testing

This document outlines the unit testing approach for `SecurityUtils.java` located in `utils/`.

---

## Status: Fully Implemented in `SecurityUtilsTest.java`

---

## Objective

Ensure that the `SecurityUtils` class:

- Hashes passwords securely using SHA-256 with cryptographically random salts
- Verifies hashed passwords accurately using constant-time comparison
- Produces different hashes for the same password when using different salts
- Produces the same hash when using the same password and the same salt
- Handles salt generation and hashing without using deprecated or unsafe APIs

---

## Tests Implemented

### 1. `testGenerateSaltProducesUniqueNonNullString`
- Verifies that `generateSalt()` returns non-null, non-blank strings
- Asserts that multiple invocations produce different values

### 2. `testHashPasswordProducesNonNullHash`
- Hashes a password using a generated salt
- Asserts the result is non-null, non-empty, and different from the plain text password

### 3. `testVerifyPasswordSuccess`
- Hashes a password using a salt
- Verifies that the password matches the stored hash
- Asserts that `verifyPassword(...)` returns `true`

### 4. `testVerifyPasswordFailsWithWrongPassword`
- Hashes a correct password using a salt
- Attempts verification using a wrong password with the same salt
- Asserts that verification returns `false`

### 5. `testVerifyPasswordFailsWithWrongSalt`
- Hashes a password with one salt
- Attempts verification using a different salt
- Asserts that verification fails

### 6. `testHashPasswordConsistencyWithSameInputFails`
- Hashes the same password twice using different salts
- Asserts that the resulting hashes are not equal

---

## Design Notes

- Salt generation is handled using `SecureRandom` and encoded with Base64
- Hashing uses `MessageDigest` with SHA-256 as specified in `ServerConstants`
- Constant-time comparison is used via `MessageDigest.isEqual(...)`
- Tests avoid assumptions about internal formatting (e.g., no salt-hash colon format)
- Utility class instantiation is safely disabled with a private constructor

---

## Sample Assertions (JUnit)

```java
String password = "MySecurePassword123!";
String salt = SecurityUtils.generateSalt();
String hash = SecurityUtils.hashPassword(password, salt);

assertNotNull(hash);
assertFalse(hash.isBlank());
assertTrue(SecurityUtils.verifyPassword(password, hash, salt));
