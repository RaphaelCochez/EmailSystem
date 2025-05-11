# CommandHandler Testing

This document outlines the unit testing strategy for the `CommandHandler` class in `server/handler/CommandHandler.java`.

---

## Objective

Ensure that the `CommandHandler` class:

* Properly routes protocol commands to appropriate services
* Responds using protocol-compliant messages
* Handles malformed input and unknown commands gracefully
* Respects access control for email reading
* Supports registration, login, logout, and session lifecycle tracking
* Handles retrieval and search of emails for authenticated users

---

## Tests Implemented

### 1. Unknown Command

* Send a command that is not defined in the protocol
* Expect response: `UNKNOWN_COMMAND%%...`

### 2. Invalid Format

* Send input without the `%%` delimiter
* Expect response: `INVALID_FORMAT%%Missing delimiter`

### 3. Send Email to Registered User

* Register a recipient
* Send valid email JSON
* Expect response: `SEND_EMAIL_SUCCESS`

### 4. Read Email Unauthorized

* Save email to database
* Attempt to access it from a user that is neither sender nor recipient
* Expect response: `UNAUTHORIZED%%...`

### 5. Register New User

* Send valid user JSON
* Expect response: `REGISTER_SUCCESS`

### 6. Register Duplicate User

* Save user manually
* Attempt to register again
* Expect response: `REGISTER_FAIL%%...`

### 7. Login with Correct Credentials

* Register then login using the same credentials
* Expect response: `LOGIN_SUCCESS`

### 8. Login with Wrong Password

* Register a user
* Attempt login with incorrect password
* Expect response: `LOGIN_FAIL%%Invalid credentials`

### 9. Logout Flow

* Register, login, then logout
* Expect response: `LOGOUT_SUCCESS`

### 10. Retrieve Received Emails

* Save a received email for a user
* Start session and call `RETRIEVE_EMAILS`
* Expect response: `RETRIEVE_EMAILS_SUCCESS%%[...]`

### 11. Search Emails With Match

* Save an email containing a search keyword
* Start session and call `SEARCH_EMAIL`
* Expect response: `SEARCH_EMAIL_SUCCESS%%[...]`

---

## Sample Assertions (JUnit)

```java
handler.handle("REGISTER%%" + gson.toJson(user), dummySocket, out);
assertTrue(writer.toString().contains("REGISTER_SUCCESS"));

handler.handle("LOGIN%%" + gson.toJson(user), dummySocket, out);
assertTrue(writer.toString().contains("LOGIN_SUCCESS"));

handler.handle("LOGOUT%%{\"email\":\"logout@example.com\"}", dummySocket, out);
assertTrue(writer.toString().contains("LOGOUT_SUCCESS"));
```

---

## Summary

These tests verify the full request-response lifecycle of the `CommandHandler`, including:

* Protocol decoding
* Delegation to the correct service
* JSON validation
* Output formatting
* Session tracking
* Authenticated email operations

With all tests passing, the handler is considered stable and protocol-compliant.
