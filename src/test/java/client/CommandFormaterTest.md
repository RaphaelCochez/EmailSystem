# CommandFormatter Test Documentation

This file outlines the unit testing strategy and coverage for `CommandFormatter.java`.

---

## Objective

Ensure that the `CommandFormatter`:
- Correctly formats command constants and JSON payloads.
- Handles empty input, nulls, and escape characters.
- Returns expected output for each command type.

---

## Test Class

**File:**  
src/test/java/client/CommandFormaterTest.java

---

## Tests Implemented

### 1. `testFormatRegisterCommand`
- Command: `REGISTER`
- Payload: `{ "email": "a@b.com", "password": "secret" }`
- Asserts correct formatted string `REGISTER%%{...}`

### 2. `testFormatLoginCommand`
- Command: `LOGIN`
- Payload: `{ "email": "x@x.com", "password": "1234" }`
- Asserts correct format.

### 3. `testFormatSendEmailCommand`
- Command: `SEND_EMAIL`
- Payload: `{ "to": "a@b.com", "subject": "hi", "body": "hello" }`
- Ensures all fields are captured.

### 4. `testFormatWithEmptyPayload`
- Command: `EXIT`
- Payload: empty map
- Asserts: `EXIT%%{}`

### 5. `testFormatNullKeyOrValue`
- Provides `null` in map keys or values
- Ensures `null` is properly encoded in JSON (or throws safely)

---

## Notes

- JSON escaping was validated using `Gson` standard behavior.
- `%%` delimiter is hard-coded and must be respected.
- Future enhancements may include schema validation for each payload.
