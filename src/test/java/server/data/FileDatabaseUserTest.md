# FileDatabase User Storage Testing

This document outlines the unit testing strategy for user storage operations in `FileDatabase.java`.

---

## Status: ✅ Fully Implemented in `FileDatabaseUserTest.java`

---

## Objective

Ensure that the `FileDatabase` class:

- Saves user data correctly in memory using `saveUser(...)`
- Retrieves users accurately via `getUser(...)`
- Prevents duplicate user entries (same email)
- Supports persistent user storage to disk with `saveAll()`
- Reloads user data after shutdown using `loadAll()`
- Tracks total user count correctly with `getUserCount()`

---

## Tests Implemented

### 1. `testSaveAndRetrieveUser`
- Creates a `User` object and saves it
- Retrieves the same user with `getUser(...)`
- Asserts all fields (email, password) match

### 2. `testDuplicateUserIsNotSaved`
- Saves a user with a specific email
- Attempts to save the same user again
- Verifies the second save is rejected
- Confirms the original data is preserved

### 3. `testSaveUserAndReloadFromDisk`
- Saves a user and flushes data to disk using `saveAll()`
- Loads a new `FileDatabase` instance from the test file
- Asserts that the user is still accessible after reload

### 4. `testUserCountReflectsSavedUsers`
- Asserts initial user count is zero
- Saves two unique users
- Asserts `getUserCount()` returns the expected value

---

## Setup Details

- Uses isolated `.db` files under `src/test/resources/`:
  - `test_users.db`
  - `test_emails.db`
- Files are deleted and recreated before each test
- Cleanup is performed after each run to avoid contamination
- Each test operates with a fresh in-memory and on-disk environment

---

## Sample Assertions (JUnit)

```java
User user = new User("test@example.com", "hashedpw");
assertTrue(fileDatabase.saveUser(user));

fileDatabase.saveAll();

FileDatabase reloaded = new FileDatabase("src/test/resources/test_users.db", "src/test/resources/test_emails.db");
reloaded.loadAll();

User loaded = reloaded.getUser("test@example.com");
assertNotNull(loaded);
assertEquals("hashedpw", loaded.getPassword());
