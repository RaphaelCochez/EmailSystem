# Logging Handler Testing

This document outlines the unit testing approach for `LogHandler.java` located in `utills/`.

---

## Objective

Ensure that the `LogHandler` class:
- Correctly writes log messages to `logs/server.log`
- Provides non-blocking, asynchronous logging
- Allows safe shutdown of the logging thread pool
- Prints selected logs to the terminal using `logAndPrint()`

---

## Tests Implemented

### 1. `testLogWritesToFile`
- Log a message using `log()`
- Wait for the asynchronous task to complete
- Verify the message is written in the log file

### 2. `testLogAndPrintOutputsCorrectly`
- Log a message using `logAndPrint()`
- Wait for async task to finish
- Check log file contains the correct message

### 3. `testShutdownCleansUpExecutor`
- Shut down the logging service using `shutdown()`
- Confirm the method completes without throwing exceptions

---

## Sample Assertions (JUnit)

```java
LogHandler.log("Test message");
Thread.sleep(100); // Wait for async execution
List<String> lines = Files.readAllLines(Paths.get("logs/server.log"));

assertFalse(lines.isEmpty());
assertTrue(lines.get(0).contains("Test message"));
```