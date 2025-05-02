# Security Utilities Testing

This document outlines the unit testing approach for `SecurityUtils.java` located in `utils/`.

---

## Status: Implemented in SecurityUtilsTest.java

---

## Objective

Ensure that the `SecurityUtils` class:
- Hashes passwords securely with random salts
- Verifies hashed passwords accurately
- Produces different hashes for the same password using different salts
- Produces the same hash when using the same salt and password

---

## Tests Implemented

### 1. `testHashPasswordAndVerifySuccess`
- Generate a salt and hash a password
- Use `verifyPassword()` with the same password and salt
- Assert that verification returns `true`

### 2. `testVerifyFailsWithWrongPassword`
- Generate a salt
- Hash a correct password
- Attempt to verify with a wrong password
- Assert `false` is returned

### 3. `testHashGeneratesDifferentHashesWithDifferentSalts`
- Generate two different salts
- Hash the same password with both
- Assert that the two hashes are not equal

### 4. `testSameInputProducesSameHashWithSameSalt`
- Use a fixed password and salt
- Hash it twice
- Assert the two hashes are equal

---

## Sample Assertions (JUnit)

```java
String password = "MySecurePassword123!";
String salt = SecurityUtils.generateSalt();
String hash = SecurityUtils.hashPassword(password, salt);

assertNotNull(hash);
assertTrue(SecurityUtils.verifyPassword(password, hash, salt));
