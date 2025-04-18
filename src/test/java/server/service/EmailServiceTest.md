# EmailService Testing

This document outlines the unit testing strategy for the `EmailService` class in `server/service/EmailService.java`.

---

## Objective

Ensure that the `EmailService` class:
- Handles sending, listing, searching, and retrieving emails correctly
- Enforces validation and user access rules during send and read operations
- Operates entirely in memory, in accordance with CA2 runtime constraints

---

## Tests Implemented

### 1. Send Email to Registered User
- Save a recipient user
- Send a valid email to that user
- Assert that the email is accepted and saved

### 2. Send Email to Unregistered User
- Attempt to send an email to an unknown recipient
- Assert that the result is `false` (email rejected)

### 3. Get Received Emails
- Store an email addressed to a user
- Retrieve received emails via `getReceivedEmails`
- Assert correctness of subject and size

### 4. Get Sent Emails
- Store an email sent by a user
- Retrieve sent emails via `getSentEmails`
- Assert correctness of subject and size

### 5. Search Emails (Received)
- Save an email with keyword in subject
- Search by keyword in received context
- Assert one result and correct ID

### 6. Search Emails (Sent)
- Save an email with keyword in recipient
- Search by keyword in sent context
- Assert one result and correct ID

### 7. Get Email by ID (Authorized)
- Save an email where requester is the recipient
- Fetch by ID using `getEmailById`
- Assert all expected fields match

### 8. Get Email by ID (Unauthorized)
- Save an email not linked to requester
- Fetch by ID using `getEmailById`
- Assert result is `null`

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

List<Email> received = emailService.getReceivedEmails("user@example.com");
assertEquals(1, received.size());
assertEquals("Welcome", received.get(0).getSubject());

Email retrieved = emailService.getEmailById("user@example.com", received.get(0).getId());
assertNotNull(retrieved);
```

