# EmailService Testing

This document outlines the unit testing strategy for the `EmailService` class in `server/service/EmailService.java`.

---

## Status: Fully Implemented in `EmailServiceTest.java`

---

## Objective

Ensure that the `EmailService` class:

* Handles sending, listing, searching, and retrieving emails correctly
* Enforces validation rules (presence of recipient, subject, body, etc.)
* Prevents sending to unknown recipients
* Logs all actions using SLF4J and LogHandler
* Validates sender or recipient identity when fetching email by ID

---

## Tests Implemented

### 1. `testSendEmailToRegisteredUser`

* Saves a valid recipient user
* Sends an email with required fields
* Asserts that the email is accepted, saved, and assigned a unique ID

### 2. `testSendEmailToUnregisteredUserFails`

* Sends an email to a recipient not in the database
* Asserts that `sendEmail` returns `false`

### 3. `testGetReceivedEmails`

* Saves a received email for a user
* Calls `getReceivedEmails`
* Asserts list size and subject content

### 4. `testGetSentEmails`

* Saves a sent email from a user
* Calls `getSentEmails`
* Asserts list size and subject content

### 5. `testSearchEmailsReceived`

* Saves an email with keyword in subject/body
* Calls `searchEmails(..., "received", keyword)`
* Verifies correct match and ID

### 6. `testSearchEmailsSent`

* Saves a sent email with identifiable keyword
* Calls `searchEmails(..., "sent", keyword)`
* Verifies correct match and ID

### 7. `testGetEmailByIdSuccess`

* Saves an email linked to the user (sent or received)
* Calls `getEmailById(user, emailId)`
* Asserts all fields match expected values

### 8. `testGetEmailByIdUnauthorized`

* Saves an email unrelated to the querying user
* Asserts that `getEmailById` returns `null`

---

## Sample Assertions (JUnit)

```java
EmailService emailService = new EmailService(fileDatabase);
Email email = new Email();
email.setTo("user@example.com");
email.setFrom("admin@example.com");
email.setSubject("Welcome");
email.setBody("Thanks for joining.");
email.setTimestamp("2025-04-18T12:00:00Z");

assertTrue(emailService.sendEmail(email));
assertNotNull(email.getId());

List<Email> received = emailService.getReceivedEmails("user@example.com");
assertEquals(1, received.size());
assertEquals("Welcome", received.get(0).getSubject());

Email retrieved = emailService.getEmailById("user@example.com", received.get(0).getId());
assertNotNull(retrieved);
assertEquals("admin@example.com", retrieved.getFrom());
```

---

## File Path

```text
src/test/java/server/service/EmailServiceTest.java
```

**Related Classes:**

* `server.service.EmailService`
* `server.data.FileDatabase`
* `model.Email`
* `model.User`
* `utils.LogHandler`
