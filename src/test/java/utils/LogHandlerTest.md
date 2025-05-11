# LogHandler Unit Testing

This document outlines the unit testing strategy for the `LogHandler` utility in the `utils` package.

---

## Status: Fully Implemented in `LogHandlerTest.java` (Java 17+ compatible)

---

## Objective

Ensure that `LogHandler`:

- Asynchronously writes logs to the file defined in `ServerConstants.LOG_FILE_PATH`
- Captures `INFO`, `WARN`, and `ERROR` level logs
- Flushes log messages correctly before and after shutdown
- Allows test-safe executor resets via `resetExecutorForTests()`
- Avoids use of reflection, static overrides, or unsafe modifications

---

## Tests Implemented

### 1. `testInfoLogIsWrittenToFile`
- Logs a message with `LogHandler.info(...)`
- Asserts the line is written with the `INFO` level to `server.log`

### 2. `testWarnLogIsWrittenToFile`
- Logs a message with `LogHandler.warn(...)`
- Confirms presence in the log file with `WARN` level

### 3. `testErrorLogIsWrittenToFile`
- Logs a message with `LogHandler.error(...)`
- Verifies that the log file contains the correct `ERROR` entry

### 4. `testLogHandlerShutdownIsNonBlocking`
- Verifies that `LogHandler.shutdown()` executes without exception
- Confirms graceful termination of the executor service

---

## Design Notes

- **Async flush handling**: Tests include a `Thread.sleep()` to allow asynchronous log writing to complete
- **Executor reset**: `LogHandler.resetExecutorForTests()` is used in `@BeforeEach` to isolate test environments
- **File isolation**: Log file is cleaned up after each test to ensure consistency
- **No reflection used**: Tests do not manipulate static final fields or constants
- **Safe for Java 17+**: All APIs used are compatible with current platform restrictions

---

## Sample Assertion

```java
LogHandler.info("Test message");
Thread.sleep(200); // allow async flush
List<String> lines = Files.readAllLines(Path.of("logs", "server.log"));
assertTrue(lines.stream().anyMatch(line -> line.contains("Test message")));
