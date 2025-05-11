# FileDatabase Email Storage Testing

This document outlines the unit testing approach for `FileDatabase.java`, focused on email storage, retrieval, search, and persistence.

---

## Status: ✅ Fully Implemented in `FileDatabaseEmailTest.java`

---

## Objective

Ensure that the `FileDatabase` class:

- Correctly stores `Email` objects in memory
- Retrieves emails using `getEmailsForUser(email, sent)`
- Saves email data to disk using `saveAll()`
- Reloads email data from disk using `loadAll()`
- Finds emails by ID with `getEmailById(...)`
- Performs keyword-based search via `searchEmails(...)`
- Gracefully handles unknown or empty queries

---

## Tests Implemented

### 1. `testSaveRetrieveAndReloadEmail`
- Creates a valid `Email` with all required fields
- Saves the email in memory via `addEmail(...)`
- Verifies presence using `getEmailsForUser(...)`
- Calls `saveAll()` to persist to file
- Reloads using a new `FileDatabase` instance
- Confirms the email is still retrievable

### 2. `testGetEmailByIdReturnsCorrectEmail`
- Adds an email with a known ID
- Fetches it using `getEmailById(...)`
- Asserts the subject and identity match

### 3. `testSearchEmailsReturnsMatchingResult`
- Adds an email with a specific keyword in subject/body
- Uses `searchEmails(...)` to locate it
- Verifies the email is found using correct query context

### 4. `testGetEmailsForUnknownUserReturnsEmpty`
- Attempts to retrieve emails for a non-existent user
- Asserts the result is a valid but empty list

---

## Helper Methods

### `createSampleEmail(to, from, subject)`
- Generates a valid `Email` instance with unique UUID and defaults

### `assertEmailEquals(expected, actual)`
- Compares all fields: ID, from, to, subject, body, timestamp, visible, edited

---

## Test Setup

- Uses isolated temporary `.db` files under `src/test/resources/`
- Deletes and recreates `test_users.db` and `test_emails.db` before each test
- Cleans up files after test execution to avoid pollution

---

## Sample Assertion (JUnit)

```java
List<Email> emails = fileDatabase.getEmailsForUser("recipient@example.com", false);
assertEquals(1, emails.size());
assertEquals("Hello", emails.get(0).getSubject());
