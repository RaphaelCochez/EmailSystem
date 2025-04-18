# FileDatabase Test Documentation

This document outlines the unit tests for the `FileDatabase.java` class. These tests verify that user and email data are correctly loaded into memory on server startup and saved to disk on shutdown, in compliance with CA2's memory-only runtime requirement.

---

## Testing Framework

All tests are written using **JUnit 5 (Jupiter)**.

### Annotations Used

- `@BeforeEach` — Sets up the environment before each test.
- `@AfterEach` — Cleans up resources after each test.
- `@Test` — Marks a method as a test case.

### Assertions

JUnit’s `Assertions` API is used to verify behavior:

- `assertTrue` / `assertFalse` — Check boolean conditions.
- `assertEquals` — Confirm expected value equality.
- `assertNotNull` — Ensure a value is not `null`.

All assertions are imported from:

```java
import static org.junit.jupiter.api.Assertions.*;
```

---

## Test Environment Setup

Each test uses temporary `.db` files created in `src/test/resources`:

- `test_users.db`
- `test_emails.db`

These files are created and cleaned up using `@BeforeEach` and `@AfterEach` to ensure test isolation. The `FileDatabase` constructor accepts these paths, and `loadAll()` is invoked to preload the in-memory state. No file access occurs during test execution.

---

## Email Tests

### `testSaveAndRetrieveEmailFromMemory`

This test verifies that an email saved using `saveEmail(...)` is stored in memory and retrievable with `getEmailsForUser(...)`.

#### What It Verifies

- Email objects are held in memory (not written during runtime).
- Retrieval correctly matches recipient address.
- All fields are preserved between save and lookup.

#### Why It Matters

Email delivery and inbox functionality rely on accurate and accessible in-memory email storage.

#### Test Logic Summary

1. Prepare test `.db` files.
2. Create a complete `Email` object.
3. Save to memory using `saveEmail(...)`.
4. Retrieve using `getEmailsForUser(...)`.
5. Assert all expected values match.

#### Code Excerpt

```java
Email email = new Email();
email.setId(UUID.randomUUID().toString());
email.setTo("recipient@example.com");
email.setFrom("sender@example.com");
email.setSubject("Hello");
email.setBody("Message content here");
email.setTimestamp("2025-04-10T14:00:00Z");
email.setVisible(true);
email.setEdited(false);

boolean saved = fileDatabase.saveEmail(email);
assertTrue(saved, "Email should be saved successfully");

List<Email> loaded = fileDatabase.getEmailsForUser("recipient@example.com", "received");
assertEquals(1, loaded.size(), "One email should be retrieved");

Email loadedEmail = loaded.get(0);
assertEquals(email.getSubject(), loadedEmail.getSubject());
```

---

## User Tests

### `testSaveAndRetrieveUser`

This test ensures that a user added to memory using `saveUser(...)` can be looked up via their email address.

#### What It Verifies

- The user is only saved in memory.
- Retrieval works using the `getUser(...)` method.
- Field values remain consistent after saving.

#### Why It Matters

Authentication and login functionality depend on the correct handling of user credentials in memory.

#### Test Logic Summary

1. Create a `User` with email and password.
2. Save it using `saveUser(...)`.
3. Retrieve it using `getUser(...)`.
4. Assert values match expectations.

#### Code Excerpt

```java
User user = new User("test@example.com", "hashedpassword123");

boolean saved = fileDatabase.saveUser(user);
assertTrue(saved, "User should be saved");

User retrieved = fileDatabase.getUser("test@example.com");
assertNotNull(retrieved, "User should be found");
assertEquals("test@example.com", retrieved.getEmail());
assertEquals("hashedpassword123", retrieved.getPassword());
```

---

## Summary

These tests ensure that the `FileDatabase` class complies with the CA2 requirement to operate fully in memory during runtime. Data is loaded from disk at startup and saved only at shutdown, with no runtime file access permitted. Each test validates the correctness of core operations related to user and email state management.

