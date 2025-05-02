# LogHandler Unit Testing

This document outlines the unit testing strategy for the `LogHandler` utility in the `utils` package.

---

## Status: ✅ Fully Implemented in LogHandlerTest.java (Java 17+ compatible)

---

## Objective

Ensure that `LogHandler`:
- Asynchronously writes logs to the file defined in `Constants.LOG_FILE_PATH`
- Prints logs to the terminal when `logAndPrint()` is called
- Flushes all logs properly during shutdown
- Supports safe testing without reflection or static overrides

---

## Tests Implemented

### 1. `testLogFileCreation`
- Verifies that the log file is created during test setup

### 2. `testSimpleLogEntry`
- Logs a single message using `log()`
- Asserts that it is written to the log file after async flush

### 3. `testLogAndPrintEntry`
- Logs a message using `logAndPrint()`
- Verifies the file contains the printed message

### 4. `testMultipleLogEntries`
- Logs multiple lines
- Verifies all lines are present in the output file

### 5. `testShutdownFlushesLogs`
- Logs a message
- Shuts down the executor
- Ensures the message is still written to file after shutdown

---

## Design Notes

- **Executor reset**: `LogHandler.resetExecutorForTests()` is called in `@BeforeAll`
- **Reflection removed**: Does not override `Constants.LOG_FILE_PATH` using `setFinalStatic(...)`
- **Safe for Java 17+**: No access to internal `modifiers` field or restricted APIs

---

## Sample Assertion

```java
LogHandler.log("Test message");
TimeUnit.MILLISECONDS.sleep(300);
assertTrue(Files.readString(Paths.get("logs/server.log")).contains("Test message"));
