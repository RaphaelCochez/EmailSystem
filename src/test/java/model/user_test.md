# User Model Testing

This document outlines the unit testing approach for the `User` class in `model/User.java`.

---

## Objective

Ensure that the `User` class:
- Correctly stores and retrieves email and hashedPassword fields
- Works with Gson serialization/deserialization
- Supports basic field mutation via setters

---

## Tests to Implement

### 1. Constructor Test
- Create a `User` with parameters
- Assert values are set correctly

### 2. Getter/Setter Test
- Set email and password
- Get them and assert correctness

### 3. Gson Serialization Test
- Convert a `User` to JSON using Gson
- Deserialize back and compare fields

---

## Sample Assertions (JUnit)

```java
User user = new User("test@example.com", "abc123hash");
assertEquals("test@example.com", user.getEmail());
assertEquals("abc123hash", user.getHashedPassword());
```
