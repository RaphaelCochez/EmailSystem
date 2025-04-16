# Security Utilities Testing

This document outlines the unit testing approach for `SecurityUtills.java` located in `utills/`.

---

## Objective

Ensure that the `SecurityUtills` class:
- Hashes passwords with random salts
- Verifies hashed passwords accurately
- Produces different hashes for the same password using different salts

---

## Tests to Implement

### 1. testHashPasswordAndVerifySuccess
- Generate a salt and hash a password
- Use `verifyPassword()` with the same password and salt
- Assert that verification returns `true`

### 2. testVerifyFailsWithWrongPassword
- Generate a salt
- Hash a correct password
- Attempt to verify with a wrong password and assert `false` is returned

### 3. testHashGeneratesDifferentHashesWithDifferentSalts
- Generate two different salts
- Hash the same password with both
- Assert that the two hashes are not equal

---

## Sample Assertions (JUnit)

```java
String password = "MySecurePassword123!";
String salt = SecurityUtills.generateSalt();
String hash = SecurityUtills.hashPassword(password, salt);

assertNotNull(hash);
assertTrue(SecurityUtills.verifyPassword(password, salt, hash));
```