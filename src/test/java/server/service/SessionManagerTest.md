# SessionManager Testing

This document outlines the unit testing strategy for the `SessionManager` class in `server/service/SessionManager.java`.

---

## Status: ✅ Implemented in SessionManagerTest.java

---

## Objective

Ensure that the `SessionManager` class:
- Tracks active sessions via email-to-socket mapping
- Allows users to start and end sessions
- Supports session lookup and verification

---

## Tests Implemented

### 1. `testStartSession`
- Start a session using `startSession(email, socket)`
- Assert that `isLoggedIn(email)` returns `true`

### 2. `testGetSessionSocket`
- After starting a session, retrieve the socket using `getSessionSocket(email)`
- Assert the socket matches the original

### 3. `testEndSession`
- Start a session, then end it using `endSession(email)`
- Assert that `isLoggedIn(email)` returns `false`
- Assert that `getSessionSocket(email)` returns `null`

### 4. `testIsLoggedInFalseByDefault`
- Check `isLoggedIn()` on an email that never started a session
- Assert it returns `false`


### 5. `testClearAllSessions`
- Start multiple sessions
- Call `clearAllSessions()`
- Assert none remain logged in

(Not yet implemented — recommended)

---

## Sample Assertions (JUnit)

```java
SessionManager sessionManager = new SessionManager();
Socket dummySocket = new Socket();

sessionManager.startSession("user@example.com", dummySocket);
assertTrue(sessionManager.isLoggedIn("user@example.com"));

Socket retrieved = sessionManager.getSessionSocket("user@example.com");
assertEquals(dummySocket, retrieved);

sessionManager.endSession("user@example.com");
assertFalse(sessionManager.isLoggedIn("user@example.com"));
assertNull(sessionManager.getSessionSocket("user@example.com"));
