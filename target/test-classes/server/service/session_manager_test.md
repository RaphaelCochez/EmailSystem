# SessionManager Testing

This document outlines the unit testing strategy for the `SessionManager` class in `server/service/SessionManager.java`.

---

## Objective

Ensure that the `SessionManager` class:
- Properly manages user login sessions via socket associations
- Supports login, logout, and active session tracking
- Handles socket-to-email and email-to-socket mapping accurately

---

## Tests Implemented

### 1. Login and IsLoggedIn Test
- Log in a user with a mocked socket
- Assert that `isLoggedIn(email)` returns `true`

### 2. Get Socket by Email Test
- After login, retrieve the socket associated with a given email
- Assert that the returned socket matches the original

### 3. Logout Test
- Log in a user, then log them out
- Assert that they are no longer considered logged in

### 4. Get Email by Socket Test
- After login, retrieve the email based on the socket object
- Assert that the correct email is returned

---

## Sample Assertions (JUnit)

```java
SessionManager sessionManager = new SessionManager();
Socket mockSocket = mock(Socket.class);

sessionManager.login("user@example.com", mockSocket);

assertTrue(sessionManager.isLoggedIn("user@example.com"));
assertEquals(mockSocket, sessionManager.getSocket("user@example.com"));
assertEquals("user@example.com", sessionManager.getEmail(mockSocket));

sessionManager.logout("user@example.com");
assertFalse(sessionManager.isLoggedIn("user@example.com"));
