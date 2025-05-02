# FileDatabase User Storage Testing

This document outlines the unit testing strategy for user storage operations in `FileDatabase.java`.

---

## Status: Implemented in FileDatabaseUserTest.java

---

## Objective

Ensure that the `FileDatabase` class:
- Saves user data correctly to memory
- Prevents duplicate user entries
- Supports persistent user storage to disk
- Can reload user data after shutdown using `loadAll()`

---

## Tests Implemented

### 1. `testSaveAndRetrieveUser`
- Saves a user using `saveUser()`
- Retrieves the user using `getUser()`
- Asserts that data fields match

### 2. `testDuplicateUserIsNotSaved`
- Saves a user
- Attempts to save the same user again
- Asserts that the second save fails

### 3. `testSaveUserAndReloadFromDisk`
- Saves a user and calls `saveAll()`
- Creates a new `FileDatabase` instance
- Calls `loadAll()` and verifies user can still be retrieved

---

## Setup Details

- Uses isolated `.db` files: `test_users.db` and `test_emails.db`
- Files are recreated before each test and deleted after each run
- Ensures no interference with production resources

---

## Sample Assertions (JUnit)

```java
User user = new User("test@example.com", "hashedpw");
assertTrue(fileDatabase.saveUser(user));

fileDatabase.saveAll();

FileDatabase reloaded = new FileDatabase("test_users.db", "test_emails.db");
reloaded.loadAll();
User loaded = reloaded.getUser("test@example.com");

assertNotNull(loaded);
assertEquals("hashedpw", loaded.getPassword());
