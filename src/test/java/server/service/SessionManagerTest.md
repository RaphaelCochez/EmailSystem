# AuthService Testing

This document outlines the unit testing strategy for the `AuthService` class in `server/service/AuthService.java`.

---

## Status: Fully Implemented in `AuthServiceTest.java`

---

## Objective

Ensure that the `AuthService` class:

* Registers users securely by hashing and salting passwords
* Rejects duplicate or malformed registration attempts
* Verifies login credentials against stored hashed passwords
* Initiates and ends authenticated sessions via `SessionManager`
* Handles logout requests gracefully
* Produces appropriate protocol responses using `ProtocolConstants`

---

## Tests Implemented

### 1. `testHandleRegisterSuccess`

* Registers a new user with valid email and password
* Verifies that the registration response is `REGISTER_SUCCESS`
* Ensures the user is stored in the fake database

### 2. `testHandleRegisterDuplicateUser`

* Inserts a pre-existing user with hashed credentials
* Attempts to register with the same email again
* Verifies that the response starts with `REGISTER_FAIL`
* Confirms that a duplicate was not added

### 3. `testHandleRegisterMissingFields`

* Sends a registration payload missing the password field
* Asserts the result starts with `REGISTER_FAIL`
* Checks for an informative error message

### 4. `testHandleRegisterInvalidJson`

* Sends malformed JSON to `handleRegister`
* Verifies graceful handling and correct failure message

### 5. `testHandleLoginSuccess`

* Saves a user with a hashed password
* Attempts login with correct raw password
* Expects `LOGIN_SUCCESS` and session creation

### 6. `testHandleLoginInvalidPassword`

* Saves a user with a hashed password
* Attempts login with the wrong password
* Verifies that login fails with `LOGIN_FAIL`

### 7. `testHandleLoginNonexistentUser`

* Sends login request for an email not in the database
* Asserts that login fails with `LOGIN_FAIL` and user not found message

### 8. `testHandleLogoutSuccess`

* Starts a session for a user
* Sends a logout request
* Verifies that session is removed and `LOGOUT_SUCCESS` is returned

### 9. `testHandleLogoutNotLoggedIn`

* Sends logout request for a user who was never logged in
* Asserts that it still returns `LOGOUT_SUCCESS` without error

---

## Sample Assertions (JUnit)

```java
User user = new User("login@example.com", "Secret123");
String salt = SecurityUtils.generateSalt();
String hash = SecurityUtils.hashPassword("Secret123", salt);
fakeDatabase.saveUser(new User(user.getEmail(), salt + "$" + hash));

authService.handleLogin(gson.toJson(user), new Socket(), printWriter);
String output = responseWriter.toString().trim();

assertEquals(ProtocolConstants.RESPONSE_LOGIN_SUCCESS, output);
assertTrue(fakeSessionManager.isLoggedIn(user.getEmail()));
```

---

## Test Utilities

### `FakeDatabase`

* Overrides `saveUser(...)`, `getUser(...)`, `userExists(...)`
* Stores users in an in-memory `HashMap`
* Automatically hashes plaintext passwords if not already formatted as `salt$hash`

### `FakeSessionManager`

* Overrides session handling methods with a local `Map<String, Socket>`
* Simulates basic login/logout behavior without network side effects

---

**Test Class File:**

```text
src/test/java/server/service/AuthServiceTest.java
```

**Related Classes:**

* `server.service.AuthService`
* `server.service.SessionManager`
* `server.data.FileDatabase`
* `utils.SecurityUtils`
* `utils.ProtocolConstants`
* `model.User`


