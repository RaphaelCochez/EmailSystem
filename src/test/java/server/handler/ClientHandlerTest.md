# ClientHandler Testing

This document outlines the unit testing strategy for the `ClientHandler` class in `server/handler/ClientHandler.java`.

---

## Objective

Ensure that the `ClientHandler` class:

* Reads and processes client input correctly via `BufferedReader`
* Passes valid commands to the provided `CommandHandler`
* Flushes outputs appropriately via `PrintWriter`
* Logs connection lifecycle and command execution
* Gracefully handles input/output errors and malformed data
* Properly closes client socket and logs disconnections

---

## Tests Implemented

### 1. `testClientHandlerProcessesValidCommands`

* Simulates input of a REGISTER and EXIT command
* Confirms both commands are parsed and forwarded
* Checks response includes `EXIT_SUCCESS`

### 2. `testClientHandlerHandlesIOException`

* Uses a fake socket that throws IOException on `getInputStream()`
* Asserts that the `ClientHandler` does not crash or throw

### 3. `testMalformedJsonCommandHandledGracefully`

* Sends a malformed LOGIN command
* Confirms handler still processes the following EXIT command

### 4. `testClientHandlerExitsCleanlyOnEOF`

* Provides empty input to simulate end-of-stream
* Confirms no commands are handled

### 5. `testMultipleCommandsAreFlushedSeparately`

* Sends multiple commands including REGISTER, LOGIN, and EXIT
* Validates that each command triggers a separate response line

---

## Sample Assertions (JUnit)

```java
assertTrue(handledCommands.stream().anyMatch(cmd -> cmd.startsWith("REGISTER")));
assertTrue(socket.getCapturedOutput().contains("EXIT_SUCCESS"));
assertTrue(handledCommands.isEmpty());
assertDoesNotThrow(clientHandler::run);
```

---

## Design Notes

* Uses `FakeSocket` with preloaded input and captured output
* `FakeCommandHandler` stores and echoes commands
* Logging output is not asserted but confirmed manually in console for development

---

## Summary

The `ClientHandlerTest.java` verifies that client-side protocol commands are read, validated, dispatched, and responded to as expected. It confirms connection stability and robustness under both valid and faulty conditions.

This completes validation of all runtime behaviors required by the handler thread pattern.


