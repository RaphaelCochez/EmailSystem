# ClientHandler Test Plan

This document outlines the unit testing strategy for `ClientHandler.java` in the `server/handler` package.

---

## Status: Fully Implemented in ClientHandlerTest.java

---

## Objective

Ensure that the `ClientHandler` class:
- Reads commands from a client socket
- Delegates each command to the `CommandHandler`
- Handles multi-line protocol commands correctly
- Writes responses to the socket output stream
- Handles I/O exceptions and malformed input gracefully
- Terminates cleanly on end-of-input (EOF)

---

## Tests Implemented

### 1. `testClientHandlerProcessesValidCommands`
- Simulates multiple valid commands (e.g., `REGISTER`, `EXIT`)
- Asserts each command is delegated to `CommandHandler`
- Confirms `EXIT_SUCCESS` is printed to output

### 2. `testClientHandlerHandlesIOException`
- Uses a fake socket that throws an `IOException` on input
- Asserts that `run()` does not throw and fails gracefully

### 3. `testMalformedJsonCommandHandledGracefully`
- Simulates a malformed JSON payload in a command
- Asserts that `ClientHandler` still calls `CommandHandler`
- Verifies the session exits successfully

### 4. `testMultipleCommandsAreFlushedSeparately`
- Simulates multiple commands in sequence
- Confirms each produces its own line in the output buffer
- Ensures `EXIT_SUCCESS` is included as the last response

### 5. `testClientHandlerExitsCleanlyOnEOF`
- Simulates end-of-stream (`""` input)
- Confirms `run()` exits cleanly without exceptions
- Asserts no commands were handled

---

## Test Infrastructure

- **FakeSocket**: Simulates a `Socket` with predefined input and output streams
- **FakeSocketWithError**: Throws exceptions to simulate I/O failure
- **FakeCommandHandler**: Captures and records each command string passed for inspection

---

## Sample Assertion (JUnit)

```java
String simulatedInput = "REGISTER%%{\"email\":\"test@example.com\",\"password\":\"123\"}\nEXIT%%{}\n";
FakeSocket socket = new FakeSocket(simulatedInput);
FakeCommandHandler handler = new FakeCommandHandler();

ClientHandler clientHandler = new ClientHandler(socket, handler);
clientHandler.run();

String response = socket.getCapturedOutput();
assertTrue(response.contains("EXIT_SUCCESS"));

