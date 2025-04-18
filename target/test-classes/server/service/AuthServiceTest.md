# AuthService Test Documentation

This section documents the unit tests for the `AuthService.java` class. These tests validate user authentication and session management logic, including registration, login, and logout flows.

## Testing Framework

All tests are written using **JUnit 5 (Jupiter)**.

### Annotations Used

- `@BeforeEach` — Initializes dependencies before each test.
- `@Test` — Declares a method as a test case.

### Assertions

Assertions are from JUnit’s `Assertions` class:

- `assertEquals` — Asserts that two values are equal.
- `assertTrue` / `assertFalse` — Validates boolean conditions.
- `assertNotNull` — Checks that an object is not null.

All assertions are statically imported:

```java
import static org.junit.jupiter.api.Assertions.*;
```

## Test Environment Setup

- A `FakeDatabase` is used to simulate user storage.
- A `FakeSessionManager` simulates session tracking in-memory.
- All communication is simulated with `StringWriter` and `PrintWriter`.
- GSON is used for JSON payload serialization and deserialization.

## Registration Tests

### testHandleRegisterSuccess
```java
@Test
void testHandleRegisterSuccess() {
    User newUser = new User(EMAIL, PASSWORD);
    authService.handleRegister(gson.toJson(newUser), printWriter);

    String output = responseWriter.toString().trim();
    assertEquals("REGISTER_SUCCESS", output);
    assertNotNull(fakeDatabase.getUser(EMAIL));
}
```
**Verifies:** A valid new user can be registered.

### testHandleRegisterDuplicateUser
```java
@Test
void testHandleRegisterDuplicateUser() {
    User existing = new User(EMAIL, "existing$hash");
    fakeDatabase.saveUser(existing);

    User newUser = new User(EMAIL, PASSWORD);
    authService.handleRegister(gson.toJson(newUser), printWriter);

    String output = responseWriter.toString().trim();
    assertTrue(output.startsWith("REGISTER_FAIL"));
}
```
**Verifies:** Duplicate registrations are rejected.

### testHandleRegisterMissingFields
```java
@Test
void testHandleRegisterMissingFields() {
    String payload = "{\"email\": \"incomplete@example.com\"}";
    authService.handleRegister(payload, printWriter);

    String output = responseWriter.toString().trim();
    assertTrue(output.startsWith("REGISTER_FAIL"));
    assertTrue(output.contains("Missing required"));
}
```
**Verifies:** Registration fails with incomplete data.

### testHandleRegisterInvalidJson
```java
@Test
void testHandleRegisterInvalidJson() {
    authService.handleRegister("not_json", printWriter);

    String output = responseWriter.toString().trim();
    assertTrue(output.startsWith("REGISTER_FAIL"));
    assertTrue(output.contains("Invalid JSON"));
}
```
**Verifies:** Malformed JSON is safely handled.

## Login Tests

### testHandleLoginSuccess
```java
@Test
void testHandleLoginSuccess() {
    String email = "login@example.com";
    String rawPassword = "Secret123";

    User registrationUser = new User(email, rawPassword);
    authService.handleRegister(gson.toJson(registrationUser), printWriter);
    responseWriter.getBuffer().setLength(0);

    User loginUser = new User(email, rawPassword);
    authService.handleLogin(gson.toJson(loginUser), new Socket(), printWriter);

    String output = responseWriter.toString().trim();
    assertEquals("LOGIN_SUCCESS", output);
    assertTrue(fakeSessionManager.isLoggedIn(email));
}
```
**Verifies:** A registered user can log in with correct credentials.

### testHandleLoginInvalidPassword
```java
@Test
void testHandleLoginInvalidPassword() {
    User user = new User("badpass@example.com", PASSWORD);
    authService.handleRegister(gson.toJson(user), printWriter);
    responseWriter.getBuffer().setLength(0);

    User wrong = new User("badpass@example.com", WRONG_PASSWORD);
    authService.handleLogin(gson.toJson(wrong), new Socket(), printWriter);

    String output = responseWriter.toString().trim();
    assertTrue(output.startsWith("LOGIN_FAIL"));
    assertTrue(output.contains("Invalid credentials"));
}
```
**Verifies:** Login fails when password is incorrect.

### testHandleLoginNonexistentUser
```java
@Test
void testHandleLoginNonexistentUser() {
    User ghost = new User("ghost@example.com", "boo");
    authService.handleLogin(gson.toJson(ghost), new Socket(), printWriter);

    String output = responseWriter.toString().trim();
    assertTrue(output.startsWith("LOGIN_FAIL"));
    assertTrue(output.contains("User not found"));
}
```
**Verifies:** Login fails if the user is not found.

## Logout Tests

### testHandleLogoutSuccess
```java
@Test
void testHandleLogoutSuccess() {
    User user = new User("logout@example.com", PASSWORD);
    authService.handleRegister(gson.toJson(user), printWriter);
    authService.handleLogin(gson.toJson(user), new Socket(), printWriter);

    responseWriter.getBuffer().setLength(0);
    authService.handleLogout(gson.toJson(user), printWriter);

    String output = responseWriter.toString().trim();
    assertEquals("LOGOUT_SUCCESS", output);
    assertFalse(fakeSessionManager.isLoggedIn(user.getEmail()));
}
```
**Verifies:** A logged-in user can log out.

### testHandleLogoutNotLoggedIn
```java
@Test
void testHandleLogoutNotLoggedIn() {
    User ghost = new User("ghost@example.com", "nope");
    authService.handleLogout(gson.toJson(ghost), printWriter);

    String output = responseWriter.toString().trim();
    assertEquals("LOGOUT_SUCCESS", output); // <- Expect success regardless
}
```
**Verifies:** Logout request from a non-logged-in user still returns success.

## Test Helpers

### FakeDatabase
```java
static class FakeDatabase extends FileDatabase {
    private final Map<String, User> users = new HashMap<>();

    public FakeDatabase() {
        super("test_users.db", "test_emails.db");
    }

    @Override
    public boolean saveUser(User user) {
        if (users.containsKey(user.getEmail()))
            return false;

        String hashedPassword = user.getPassword();
        if (!hashedPassword.contains("$")) {
            String salt = SecurityUtills.generateSalt();
            String hash = SecurityUtills.hashPassword(hashedPassword, salt);
            hashedPassword = salt + "$" + hash;
        }

        User securedUser = new User(user.getEmail(), hashedPassword);
        users.put(user.getEmail(), securedUser);
        return true;
    }

    @Override
    public User getUser(String email) {
        return users.get(email);
    }
}
```

### FakeSessionManager
```java
class FakeSessionManager extends SessionManager {
    private final Map<String, Socket> sessions = new HashMap<>();

    @Override
    public void startSession(String email, Socket socket) {
        sessions.put(email, socket);
    }

    @Override
    public void endSession(String email) {
        sessions.remove(email);
    }

    @Override
    public boolean isLoggedIn(String email) {
        return sessions.containsKey(email);
    }

    @Override
    public Socket getSessionSocket(String email) {
        return sessions.get(email);
    }
}
```

---

**Test Class File:**
```text
src/test/java/server/service/AuthServiceTest.java
```

**Related Classes:**
- `server.service.AuthService`
- `server.service.SessionManager`
- `server.data.FileDatabase`
- `utills.SecurityUtills`
- `model.User`

