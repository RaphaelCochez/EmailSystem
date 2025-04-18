# ClientHandler Testing

This document outlines the unit testing strategy for the `ClientHandler` class in `server/handler/ClientHandler.java`.

---

## Objective

Ensure that the `ClientHandler` class:
- Handles client socket communication in an isolated thread
- Delegates command processing to `CommandHandler`
- Processes multiple commands per session
- Produces valid output on output stream
- Cleans up gracefully after client disconnects or errors

---

## Tests Implemented

### 1. Valid Command Execution
- Simulate input of two commands: `REGISTER%%...` and `EXIT%%{}`
- Use a mock socket and a fake command handler
- Assert that:
  - `REGISTER` command is parsed and handled
  - `EXIT` command is parsed and handled
  - Output stream includes `EXIT_SUCCESS`

### 2. Exception Handling
- Simulate a socket that throws an IOException on input stream
- Ensure `ClientHandler.run()` does not crash
- Validate no unhandled exceptions are thrown

---

## Sample Assertions (JUnit)

```java
assertTrue(registerMatched, "Expected REGISTER command with matching JSON");
assertTrue(exitMatched, "Expected EXIT command to be processed");
assertTrue(response.contains("EXIT_SUCCESS"));
```

## Fake Infrastructure

### FakeSocket
- Simulates input/output streams
- Provides dummy socket address for logging

### FakeSocketWithError
- Throws IOException on input stream to simulate client error

### FakeCommandHandler
- Records all commands handled
- Responds with command + `_RECEIVED`
- Sends `EXIT_SUCCESS` when `EXIT` command is received

---

## Summary

These tests validate that `ClientHandler`:
- Correctly reads from the socket
- Delegates command strings to a handler
- Handles disconnects and exceptions gracefully
- Works in a self-contained thread per client

All tests passing confirm correct per-client thread behavior and protocol-level compliance during client interaction.

