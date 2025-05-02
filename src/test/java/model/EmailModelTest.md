---

## `src/test/java/model/EmailModelTest.md`

# Email Model Testing

This document outlines the unit testing strategy for the `Email` class in `model/Email.java`.

---

## Status: Implemented in EmailModelTest.java

---

## Objective

Ensure the `Email` class:
- Stores all required fields
- Supports full getter/setter coverage
- Validates timestamp format
- Maintains data integrity during Gson serialization

---

## Tests Implemented

### 1. Full Field Test
- Create an `Email` object and set all fields
- Assert values match using getters

### 2. Timestamp Validation
- Set a valid ISO 8601 timestamp
- Attempt to set an invalid one and expect an exception

### 3. Visibility and Edit State
- Test boolean flags `isVisible` and `isEdited`

### 4. Gson Round Trip
- Serialize an `Email` to JSON
- Deserialize it back and compare each field

---

## Sample Assertions (JUnit)

```java
Email email = new Email();
email.setId("uuid-123");
email.setTo("to@example.com");
email.setFrom("from@example.com");
email.setSubject("Test Subject");
email.setBody("Email body");
email.setTimestamp("2025-05-01T10:00:00Z");

assertEquals("uuid-123", email.getId());
assertEquals("to@example.com", email.getTo());
assertEquals("2025-05-01T10:00:00Z", email.getTimestamp());
assertTrue(email.isVisible() || !email.isEdited());
```