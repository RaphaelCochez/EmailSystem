
---

##  `src/test/java/model/UserModelTest.md`
```markdown
---

## `src/test/java/model/UserModelTest.md`

# User Model Testing

This document outlines the unit testing approach for the `User` class in `model/User.java`.

---

## Status: Implemented in UserModelTest.java

---

## Objective

Ensure that the `User` class:
- Correctly stores and retrieves `email` and `password`
- Works with Gson serialization/deserialization
- Supports mutation via `setPassword`
- Correctly implements `equals()` and `hashCode()`

---

## Tests Implemented

### 1. Constructor Test
- Create a `User` with parameters
- Assert values are set correctly

### 2. Getter/Setter Test
- Set password and confirm value is updated

### 3. Equality Test
- Verify `.equals()` returns true for same email, different case
- Check `hashCode()` consistency

### 4. Gson Serialization Test
- Convert a `User` to JSON using Gson
- Deserialize back and compare fields

---

## Sample Assertions (JUnit)

```java
User user = new User("test@example.com", "abc123hash");

assertEquals("test@example.com", user.getEmail());
assertEquals("abc123hash", user.getPassword());
