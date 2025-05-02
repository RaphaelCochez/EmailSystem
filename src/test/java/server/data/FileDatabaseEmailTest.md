# FileDatabase Email Storage Testing

This document outlines the unit testing approach for `FileDatabase.java` focused on email storage and retrieval.

---

## Status: Implemented in FileDatabaseEmailTest.java

---

## Objective

Ensure that the `FileDatabase` class:
- Correctly stores `Email` objects in memory
- Retrieves emails using `getEmailsForUser(email, type)`
- Saves email data to disk using `saveAll()`
- Reloads email data from disk using `loadAll()`

---

## Tests Implemented

### 1. `testSaveRetrieveAndReloadEmail`

- Creates a new `Email` with all required fields
- Saves the email to memory using `saveEmail()`
- Verifies the email is accessible with `getEmailsForUser(...)`
- Calls `saveAll()` to persist data to disk
- Creates a new `FileDatabase` instance and calls `loadAll()`
- Verifies that the email is still available after reloading

---

## Helper Methods

### `assertEmailEquals(expected, actual)`
- Compares all `Email` fields (ID, from, to, subject, body, timestamp, visibility, edited)

---

## Test Setup

- Uses isolated `.db` files under `src/test/resources/`
- Deletes and recreates test files before each test run
- Cleans up after each test to avoid polluting the workspace

---

## Sample Assertion (JUnit)

```java
List<Email> emails = fileDatabase.getEmailsForUser("recipient@example.com", "received");
assertEquals(1, emails.size());
assertEquals("Hello", emails.get(0).getSubject());
