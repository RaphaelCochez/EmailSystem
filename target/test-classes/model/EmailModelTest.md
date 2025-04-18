
---

## `test/java/model/email_test.md`


# Email Model Testing

This document outlines the unit testing strategy for the `Email` class in `model/Email.java`.

---

## Objective

Ensure the `Email` class:
- Stores all required fields
- Supports full getter/setter coverage
- Maintains data integrity during Gson serialization

---

## Tests to Implement

### 1. Full Field Test
- Create an `Email` object and set all fields
- Assert values match

### 2. Unique ID Test
- Assign a UUID
- Ensure it persists through serialization

### 3. Gson Round Trip
- Serialize an `Email` to JSON
- Deserialize it back and compare each field

---

## Sample Assertions (JUnit)

```java
Email email = new Email();
email.setId("uuid-123");
email.setTo("to@example.com");
// ...

assertEquals("uuid-123", email.getId());
```